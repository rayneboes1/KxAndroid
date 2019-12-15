# ThreadLocal

[ThreadLocal 原理分析](https://wenhaiz.xyz/inside-thread-local)

每个线程内部都有一个 ThreadLocalMap 类型的变量，这个 ThreadLocalMap 以 ThreadLocal 为键，以 ThreadLocal 泛型声明的类型为值。

当调用 ThreadLocal get 方法时，它首先获取到当前的线程以及它的 ThreadLocalMap ，然后以自己为 Key，取出ThreadLocalMap的值，这就是线程私有的变量。

