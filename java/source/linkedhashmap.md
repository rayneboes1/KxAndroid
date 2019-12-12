# LinkedHashMap

LinkedHashMap 是一个哈希表，拥有可预测的迭代顺序。与 HashMap 不同点在于使用了一个双向链表来维护所有的 Entry，双向链表定义了哈希表的迭代顺序。

## 一个参数

LinkedHashMap 的迭代顺序有两种：节点插入顺序和节点访问循序。可通过在构造方法中传递 `boolean accessOrder`来指定。

当 `accessOrder` 为 false 时，迭代顺序为节点插入顺序；当 `accessOrder` 为 true 时，迭代顺序为节点访问顺序。

## 节点类型

继承了 HashMap 的 Node 类，并增加了前后指针，以便构造双向链表。

```java
static class Entry<K,V> extends HashMap.Node<K,V> {
        Entry<K,V> before, after;
        Entry(int hash, K key, V value, Node<K,V> next) {
            super(hash, key, value, next);
        }
    }
```

## 节点创建

在 HashMap 中，创建节点是通过 `newNode` 方法：

```java
Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
        return new Node<>(hash, key, value, next);
    }
```

而 LinkedHashMap 通过重写 HashMap 的 `newNode` 方法，创建Entry实例:

```java
@Override
Node<K,V> newNode(int hash, K key, V value, Node<K,V> e) {
        LinkedHashMap.Entry<K,V> p =
            new LinkedHashMap.Entry<K,V>(hash, key, value, e);
        linkNodeLast(p);
        return p;
}
```

## 更新迭代顺序的时机

在 HashMap 中，定义了如下三个方法:

```java
 // Callbacks to allow LinkedHashMap post-actions
    void afterNodeAccess(Node<K,V> p) { }
    void afterNodeInsertion(boolean evict) { }
    void afterNodeRemoval(Node<K,V> p) { }
```

通过注释可以看出是专门为 LinkedHashMap 进行后续行为而定义的。

`afterNodeAccess` 是节点被访问后调用，在 HashMap 中，这个方法只在更新旧节点的值时被调用了，在`get`方法内并没有被调用，因此 LinkedHashMap 重写了 `get`方法，当需要根据访问顺序迭代时，调用`afterNodeAccess` 调整：

```java
public V get(Object key) {
    Node<K,V> e;
    if ((e = getNode(hash(key), key)) == null){
        return null;
    }
    if (accessOrder){
        afterNodeAccess(e);
    }
    return e.value;
}
```

`afterNodeInsertion` 处理新增节点后行为,在 HashMap 中的 `putVal`方法中最后就调用了这个方法，但是 `afterNodeInsertion` **只处理了新节点添加之后**的顺序更新，比如如果需要删除头节点，则在此时进行删除调整；而对于新创建的节点，更新迭代顺序是在`linkNodeLast`方法中，如上面代码所示，这个方法是在`newNode`方法中被调用的。

`afterNodeRemoval` 在 HashMap 中的`removeNode`方法中调用。

综上，LinkedHashMap 更新迭代顺序链表的时机为：

| 时机 | 调用方法 |
| :---: | :---: |
| 插入新节点时 | `linkNodeLast` |
| 插入新节点后（处理删除头节点） | `afterNodeInsertion` |
| 更新旧节点 | `afterNodeAccess` |
| 删除节点 | `afterNodeRemoval` |
| 访问节点 | `afterNodeAccess` |

## 调整顺序的源码实现

### `linkNodeLast`

linkNodeLast 的作用就是将新创建的节点放置于链表尾部，其源码如下：

```java
private void linkNodeLast(LinkedHashMap.Entry<K,V> p) {
    //tail 是指向链表尾结点的引用
    LinkedHashMap.Entry<K,V> last = tail;
    tail = p;
    if (last == null){
        //链表为空时，初始化头结点
        head = p;
    }else {
        //新尾节点before指向原尾结点
        p.before = last;
        //原尾节点的after 指向的新尾节点
        last.after = p;
    }
}
```

### `afterNodeInsertion`

```java
void afterNodeInsertion(boolean evict) { // possibly remove eldest
        LinkedHashMap.Entry<K,V> first;
        // evict=true & 链表不为空 & removeEldestEntry返回 true
        if (evict && (first = head) != null && removeEldestEntry(first)) {
            K key = first.key;
            //删除头结点
            removeNode(hash(key), key, null, false, true);
        }
    }
```

`evict` 标识 HashMap 是否处于创建中，evict 为 `false` 的情况主要包括在构造方法中、`clone`、`readFromObject`等方法中调用 put 相关操作时，其他情况一律为 `true`。

可以看出 `afterNodeInsertion` 主要是根据需要移除头结点，然后在移除后会有调整顺序的操作，不过已经是在`afterNodeRemoval`方法中了。 \(`removeNode` 会调用 `afterNodeRemoval`\)

### `afterNodeRemoval`

`afterNodeRemoval` 的作用很简单，处理了节点删除后的链表维护。

```java
void afterNodeRemoval(Node<K,V> e) { // unlink
    LinkedHashMap.Entry<K,V> p =
            (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after; 
    //清理指针         
    p.before = p.after = null;
    if (b == null){
        //如果删除的是头结点，则重新设置头结点
        head = a;
    }else{
        //重新设置前驱节点的后继节点
        b.after = a;
    }
    if (a == null){
        //如果删除的是尾结点，则重新设置尾节点
        tail = b;
    }else{
        //重新设置后继节点的前驱节点
        a.before = b;
    }
}
```

### `afterNodeAccess`

`afterNodeAccess` 方法在 `accessOrder`为`true`时，将被访问的节点移到链表尾部。

```java
void afterNodeAccess(Node<K,V> e) { // move node to last
    LinkedHashMap.Entry<K,V> last;
    //accessOrder=true && 访问的不是尾节点
    if (accessOrder && (last = tail) != e) {
        LinkedHashMap.Entry<K,V> p =
                (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
        p.after = null;//清理后继节点指针，因为节点会被移到链表末尾
        if (b == null){
            //如果被访问节点是头结点，则将头结点指向头结点的后继节点
            head = a;
        }else{
            //调整前驱节点的后继节点
            b.after = a;
        }
        if (a != null){
            //如果后继节点不为空，则更新后继节点的前驱节点
            a.before = b;
        }else{
            //疑问：如果后继节点为null，则当前节点是尾结点(tail有可能为null？)，而方法开始已经加了非尾节点的判断，为什么这里又判断一次呢？
            //last 指向前驱节点
            last = b;
        }
        if (last == null){
            head = p;
        }else {
            //移到链表尾部
            p.before = last;
            last.after = p;
        }
        //更新尾节点指针
        tail = p;
        ++modCount;
    }
}
```



## 其他优化

### containsValue\(\)

LinkedHashMap 直接从头遍历双向链表查找， HashMap 遍历数组同时还要遍历每个桶内的链表，效率比LinkedHashMap 低。

```java
//LinkedHashMap 
public boolean containsValue(Object value) {
    for (LinkedHashMapEntry<K,V> e = head; e != null; e = e.after) {
       V v = e.value;
    if (v == value || (value != null && value.equals(v)))
          return true;
     }
     return false;
}

//HashMap
public boolean containsValue(Object value) {
    Node<K,V>[] tab; V v;
    if ((tab = table) != null && size > 0) {
        for (int i = 0; i < tab.length; ++i) {
            for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                if ((v = e.value) == value ||
                        (value != null && value.equals(v)))
                    return true;
            }
        }
    }
    return false;
}
```

## 总结

LinkedHashMap 是一个拥有可预测迭代顺序的哈希表，它继承自 HashMap，并用一个双向链表来维护所有 entry 的顺序，默认迭代顺序是按节点插入顺序，可以通过将`accessOrder`设为`true`将迭代顺序指定为节点访问顺序。当节点被添加、修改、访问和删除时，通过调用对应的方法来调整节点在双向链表中的顺序，从而达到维护访问顺序的目的。

