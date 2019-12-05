# 集合

### Java集合框架中有哪些类？都有什么特点？

![Java &#x96C6;&#x5408;&#x6846;&#x67B6;&#x7C7B;&#x56FE;](../.gitbook/assets/2243690-9cd9c896e0d512ed.gif)

### 集合、数组、泛型的关系，并比较

集合是包含多个对象的简单对象，所包含的对象称为元素。在 Java 中对应 Collection 接口，是一组允许重复的对象。

数组是由相同类型的若干项数据组成的一个数据集合，数组创建后在内存里面占用连续的内存地址，可通过下标对元素进行随机访问。数组是引用类型，一旦被创建，就不能更改数组的长度。

泛型提供了**编译时类型安全检测**机制，该机制允许程序员在编译时检测到非法的类型。泛型的本质是参数化类型，也就是说所操作的数据类型被指定为一个参数。

### ArrayList  和 LinkList 的区别？

ArrayList 内部使用**数组**的形式实现了存储，实现了 `RandomAccess` 接口，数组中的元素在内存中的地址是连续的，因此可以利用数组的下标进行元素的随机访问，速度非常快。

ArrayList 在初始化的时候，有初始大小10，插入新元素的时候，会判断是否需要扩容，扩容是扩容为原容量的1.5倍，扩容方式是利用数组的复制，因此有一定的开销；另外，ArrayList在进行元素插入的时候，需要移动插入位置之后的所有元素，位置越靠前，需要位移的元素越多，开销越大，相反，插入位置越靠后的话，开销就越小了，如果在最后面进行插入，那就不需要进行位移。  

LinkedList 内部使用**双向链表**的结构实现存储，LinkedList 有一个内部类作为存放元素的单元，里面有三个属性，用来存放元素本身以及前驱节点和后继节点的引用。LinkedList 每一个元素的**地址不连续**，通过引用找到当前结点的上一个结点和下一个结点，即插入和删除效率较高，时间复杂度为O\(1\)，而 get 和 set 则较为低效。另外，LinkedList 还实现了 `Deque`接口，可以用来作为队列使用  
  
当插入和删除操作较频繁时，使用 LinkedList 性能更好；当随机访问比较频繁时，使用 ArrayList 更好。

### ArrayList 和 Vector 的区别？

两者都实现了 List 接口并且都是基于数组。但是 Vector 是线程安全的，它的关键方法都通过synchronized 关键字来保证线程安全，但 ArrayList 是非线程安全的。如果没有并发要求，应该使用 ArrayList，使用 Vector 锁操作会影响性能。

ArrayList 在扩容时数组长度会增大为原来的1.5倍，但 Vector 可以通过在构造方法传入 `capacityIncrement` 来指定每次增加的长度，如果不指定默认为原来的两倍。

### HashSet 和 TreeSet 的区别？

即 HashMap 与 TreeMap 的区别。HashSet 不保证元素顺序，而 TreeSet 是可以保证元素按 key 排序的，默认是按key的自然顺序，可以在构造函数中传入 `Comparator` 来自定义顺序。

### [HashMap 和 Hashtable 的区别？](https://www.cnblogs.com/xinzhao/p/5644175.html)

#### 产生时间

HashTable产生于JDK 1.1，而HashMap产生于JDK 1.2。从时间的维度上来看，HashMap要比HashTable出现得晚一些。

#### 继承体系

两个类的继承体系有些不同。虽然都实现了Map、Cloneable、Serializable三个接口。但是HashMap继承自抽象类AbstractMap，而HashTable继承自抽象类Dictionary。

#### null 值

HashMap是支持null键和null值的，而HashTable在遇到null时，会抛出NullPointerException异常。这并不是因为HashTable有什么特殊的实现层面的原因导致不能支持null键和null值，这仅仅是因为HashMap在实现时对null做了特殊处理，将null的hashCode值定为了0，从而将其存放在哈希表的第0个bucket中。

#### 初始大小和扩容方案

HashTable默认的初始大小为11，之后每次扩充为原来的2n+1。HashMap默认的初始化大小为16，之后每次扩充为原来的2倍。还有我没列出代码的一点，就是如果在创建时给定了初始化大小，那么HashTable会直接使用你给定的大小，而HashMap会将其扩充为2的幂次方大小。

#### 线程安全

HashTable是同步的，公开的方法比如get都使用了synchronized描述符。而遍历视图比如keySet都使用了Collections.synchronizedXXX进行了同步包装。HashMap不是，也就是说HashTable在多线程使用的情况下，不需要做额外的同步，而HashMap则不行。

#### Hash 算法

Hashtable：

```text
int hash = key.hashCode();
int index = (hash & 0x7FFFFFFF) % tab.length;
```

HashMap:

```text
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}

int index = (n - 1) & hash(key)
```

### 如何保证HashMap线程安全？什么原理？

#### 使用 ConcurrentHashMap

JDK 1.7 中 ConcurrentHashMap 存储采用了分段锁技术，其中 Segment 继承于 ReentrantLock。不会像 HashTable 那样不管是 put 还是 get 操作都需要做同步处理，理论上 ConcurrentHashMap 支持 CurrencyLevel \(Segment 数组数量\)的线程并发。每当一个线程占用锁访问一个 Segment 时，不会影响到其他的 Segment。get 方法没有加锁。

JDK 1.8 中处理哈希冲突同HashMap ，抛弃了原有的 Segment 分段锁，而采用了 `CAS + synchronized` 来保证并发安全性。如果定位Node为空，则使用CAS写入数据，否则使用 synchronized 锁写入数据。

> **比较并交换\(compare and swap, CAS\)**，是原子操作的一种，可用于在多线程编程中实现不被打断的数据交换操作，从而避免多线程同时改写某一数据时由于执行顺序不确定性以及中断的不可预知性产生的数据不一致问题。 该操作通过将内存中的值与指定数据进行比较，当数值一样时将内存中的数据替换为新的值。
>
> **CAS操作基于CPU提供的原子操作指令实现**。对于Intel X86处理器，可通过在汇编指令前增加LOCK 前缀来锁定系统总线，**使系统总线在汇编指令执行时无法访问相应的内存地址**。而各个编译器根据这个特点实现了各自的原子操作函数。

[JDK 1.7 ConcurrentHashMap](https://www.ibm.com/developerworks/cn/java/java-lo-concurrenthashmap/index.html)  

[crossoverjie's blog](https://crossoverjie.top/2018/07/23/java-senior/ConcurrentHashMap/)

[维基百科:比较并交换](https://zh.wikipedia.org/wiki/%E6%AF%94%E8%BE%83%E5%B9%B6%E4%BA%A4%E6%8D%A2)

#### 使用 Collections.synchronizedMap\(oldMap\)

该方法返回一个 `SynchronizedMap` 包装对象，调用的还是原来的 HashMap ，只不过调用前给所有方法都加了互斥锁。

### 什么是 `fail-fast`  ?

如果在对集合创建迭代器之后，通过除了通过迭代器自己的remove方法之外的其他方法堆集合进行了结构性修改，那么该迭代器将抛出 `ConcurrentModificationException`。 因此，面对并发修改，迭代器会快速干净地失败，而不会在未来的不确定时间内冒不确定行为的风险。

### fail-fast 的实现机制？

通过在迭代器内比较 modCount 是否相同来完成。在创建迭代器之后，会保存集合当前的modCount，如果在创建迭代器之后，对集合结构进行修改（通常是移除元素、增加元素、扩容等），modCount 会自增，因此当迭代器发现集合的 modCount 跟创建时不一样时就会抛出 `ConcurrentModificationException` 。 

