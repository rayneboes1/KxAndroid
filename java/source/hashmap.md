# HashMap

## 定义

> Hash table based implementation of the Map interface. This implementation provides all of the optional map operations, and permits null values and the null key. \(The HashMap class is roughly equivalent to Hashtable, except that it is unsynchronized and permits nulls.\) This class makes no guarantees as to the order of the map; in particular, it does not guarantee that the order will remain constant over time.

关键描述：基于Map接口实现、允许null键/值、非同步、不保证有序\(比如插入的顺序\)、也不保证key的顺序序不随时间变化。

## 两个重要参数

* initialCapacity 哈希表底层数组的初始化容量，默认为 16
* loadFactor 负载因子，衡量数组填满程度，当hashmap中元素个数超过 Capacity\*loadFactor 时，数组容量会扩充为原来的 2 倍 

> 即使初始化时指定了非2的幂次的容量，内部也会将其变为2的幂次。

## put 函数的实现

put 方法大致的思路为：

1. 对key的hashCode\(\)做hash，然后再计算index;
2. 如果没碰撞直接放到bucket里；
3. 如果碰撞了，以链表的形式存在buckets后；
4. 如果碰撞导致链表过长\(大于等于TREEIFY\_THRESHOLD（默认是 8）\)，就把链表转换成红黑树；
5. 如果节点已经存在就替换old value\(保证key的唯一性\)
6. 如果 bucket 满了\(超过load factor\*current capacity\)，就要扩容。

```java
public V put(K key, V value) {
    // 通过 hash 方法对 key 做hash
    return putVal(hash(key), key, value, false, true);
}
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    Node<K,V>[] tab; 
    Node<K,V> p; 
    int n, i;
    // 如果 tab 为空则创建
    if ((tab = table) == null || (n = tab.length) == 0){
        n = (tab = resize()).length;
    }
    // 使用 (n - 1) & hash 计算在数组中的 index，并对 null 做处理
    if ((p = tab[i = (n - 1) & hash]) == null){
        //不存在哈希冲突，直接存入
        tab[i] = newNode(hash, key, value, null);
    } else {
        //hash 冲突的节点存在

        Node<K,V> e;//hash 冲突的节点
        K k; //hash 冲突的节点的 key   
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k)))){
            //判断 hash 和 key 是否都相同
            //如果相同需要将旧节点的值替换为新节点的值    
            e = p;
        } else if (p instanceof TreeNode){
            // 该链为树，尝试将新值插入到从红黑树中，如果存在key和hash完全相同的节点，则将其返回
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        } else {
            // 该链为链表
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    //没有相同key的节点，直接插入到链表尾部
                    p.next = newNode(hash, key, value, null);
                    if (binCount >= TREEIFY_THRESHOLD - 1){
                        //如果链表过长，将链表转化为红黑树
                        treeifyBin(tab, hash);
                    } 
                    break;
                }
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k)))){
                    //找到hash 和 key相同的旧节点
                    break;
                }
                p = e;
            }
        }
        // 如果旧结点存在，将旧节点的值修改为新值
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null){
                e.value = value;
            }
            //进行访问节点后的处理，该方法在 HashMap 中为空实现
            afterNodeAccess(e);
            return oldValue;
        }
    }
    //修改次数，用于在迭代时判断是否修改，以便触发fail-fast
    ++modCount;
    // 超过load factor*current capacity，resize
    if (++size > threshold){
        resize();
    }
    //进行插入节点后的处理，该方法在 HashMap 中为空实现
    afterNodeInsertion(evict);
    return null;
}
```

## get 函数的实现

1. 计算hash 和索引， 去找数组里的第一个节点，如果不冲突，直接命中；
2. 如果有冲突，则通过key.equals\(k\)去查找对应的entry。若为树，则在树中通过key.equals\(k\)查找，O\(logn\)；若为链表，则在链表中通过key.equals\(k\)查找，O\(n\)。

```java
public V get(Object key) {
    Node<K,V> e;
    //调用hash方法计算key的哈希值
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}
final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; 
    Node<K,V> first, e; 
    int n; 
    K k;
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (first = tab[(n - 1) & hash]) != null) {
        // 直接命中
        if (first.hash == hash && // always check first node
            ((k = first.key) == key || (key != null && key.equals(k)))){
                return first;
            }
        // 未命中,但是存在哈希冲突
        if ((e = first.next) != null) {
            // 在红黑树中 get
            if (first instanceof TreeNode){
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
            }
            // 在链表中 get
            do {
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k)))){
                    return e;
                    }
            } while ((e = e.next) != null);
        }
    }
    return null;
}
```

## hash 函数的实现

将 key 的 hashCode 高16bit不变，低16bit和高16bit做了一个异或。

```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

在设计hash函数时，因为目前的table长度n为2的幂，而计算下标的时候，是这样实现的\(使用&位操作，而非%求余\)：

```text
(n - 1) & hash
```

在 n 比较小的时候，只有hashCode 的低位参与哈希，碰撞的概率很大，而通过这种简单的方式，可以让 hashCode 的高位也参与 hash，有效避免哈希冲突，同时也不会影响性能；而对于哈希冲突过于严重的情况采用链表及红黑树\(JDK1.8\)来处理。

## resize 函数的实现

当put时，如果发现目前的bucket占用程度已经超过了Load Factor所希望的比例，那么就会发生resize。在resize的过程，简单的说就是把bucket扩充为2倍，之后重新计算index，把节点再放到新的bucket中。

由于使用的是2次幂的扩展\(指长度扩为原来2倍\)，所以，元素的位置要么是在原位置，要么是在原位置再移动2次幂的位置。

例如，如果n由16变为32时，n-1由`1111`变为`11111`，假设有两个计算好的 hash,hash1 的低五位为 01001，hash2的低五位为11001，当n=16时，两个hash计算的索引都是`1001&1111=1001`,而当n变成32后，hash1对应的index 为`01001&11111=1001(b)=9(d)`,而hash2对应的index为`11001&11111=11001=25=9+16`,向后移动的位置等于原来的容量。

因此，我们在扩充HashMap的时候，不需要重新计算hash，只需要**看看原来的hash值新增的那个bit是1还是0就好了，是0的话索引没变，是1的话索引变成“原索引+oldCap”。**

这样既省去了重新计算hash值的时间，而且同时，由于新增的1bit是0还是1可以认为是随机的，因此resize的过程，均匀的把之前的冲突的节点分散到新的bucket了。

```java
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    int oldThr = threshold;//cap*loadFactor
    int newCap, newThr = 0;
    if (oldCap > 0) {
        // 超过最大值就不再扩充了，就只好随你碰撞去吧
        //MAXIMUM_CAPACITY 为2^30
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        // 没超过最大值，就扩充为原来的2倍
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)
            newThr = oldThr << 1; // double threshold
    } else if (oldThr > 0){// initial capacity was placed in threshold
        newCap = oldThr;
    } else {               // zero initial threshold signifies using defaults
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    // 计算新的resize上限
    if (newThr == 0) {
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab;
    if (oldTab != null) {
        // 把每个bucket都移动到新的buckets中
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {
                oldTab[j] = null;
                if (e.next == null){
                    //没有hash碰撞
                    newTab[e.hash & (newCap - 1)] = e;
                } else if (e instanceof TreeNode){
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                } else { // preserve order
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    //组织成两个冲突链表，一个是就表中位置不变的元素，一个是旧表中后移oldCap个位置的元素
                    do {
                        next = e.next;
                        // 新索引为原索引的元素链表
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null){
                                loHead = e;
                            } else{
                                loTail.next = e;
                            }
                            loTail = e;
                        }
                        // 新索引为原索引+oldCap的元素链表
                        else {
                            if (hiTail == null){
                                hiHead = e;
                            } else{
                                hiTail.next = e;
                            }
                            hiTail = e;
                        }
                    } while ((e = next) != null);

                    // 原索引放到bucket里
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }

                    // 原索引+oldCap放到bucket里
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```

## 总结

HashMap 以Entry\[\]数组实现的哈希桶数组，用Key的哈希值取模桶数组的大小可得到数组下标。

插入元素时，如果两条Key落在同一个桶（比如哈希值1和17取模16后都属于第一个哈希桶），我们称之为哈希冲突。JDK 8之前的解决哈希冲突的做法是链表法，Entry 用一个next属性实现多个Entry以单向链表存放。查找哈希值为17的key时，先定位到哈希桶，然后链表遍历桶里所有元素，逐个比较其Hash值然后key值。

在JDK8里，新增默认为8的阈值，当一个桶里的Entry超过閥值，就不以单向链表而以红黑树来存放以加快Key的查找速度。

当然，最好还是桶里只有一个元素，不用去比较。所以默认当Entry数量达到桶数量的75%时，哈希冲突已比较严重，就会成倍扩容桶数组，并重新分配所有原来的Entry。扩容成本不低，所以也最好有个预估值。

取模用与操作（hash & （arrayLength-1））会比较快，所以数组的大小永远是2的N次方， 你随便给一个初始值比如17会转为32。默认第一次放入元素时的初始值是16。

## 参考

> [Java HashMap 原理和实现](https://yikun.github.io/2015/04/01/Java-HashMap%E5%B7%A5%E4%BD%9C%E5%8E%9F%E7%90%86%E5%8F%8A%E5%AE%9E%E7%8E%B0/)
>
> [Java 集合的小抄](http://calvin1978.blogcn.com/articles/collection.html)

