# 理解 volatile

## 对 volatile 的特殊规则

JVM 对 volatile 专门定义了特殊的访问规则：

* 在工作内存中，每次使用变量之前都要先从主内存刷新最新值，以保证可以看到其他线程对该变量修改的值
* 在工作内存中，每次修改变量后，都需要立刻同步回主内存，保证修改对其他线程可见
* volatile 修饰的变量不会被指令重排序优化，保证代码的执行顺序与程序的顺序相同

## volatile 的语义

### 可见性

保证变量对所有线程的可见性，可见性是指当一个线程改变了变量的值后，变量的新值在其他线程是可以立即得知的。（普通变量由于需要借助主内存在线程的工作内存中对变量进行周转，无法保证可见性）。虽然 volatile 保证了变量不会出现不一致的情况，但由于对变量执行的操作并不是都是原子性的，所以光靠 volatile 并不能保证线程安全，（例如 i++，需要先读取变量，再自增，再写回），还是需要加锁。

volatile 使用场景

```text
volatile boolean shutdown;

public void shutDown(){
    shutdown=true;
}

public void doWork(){
    while(!shutdown){
        //do stuff
    }
}
```

### 禁止指令重排序

使用volatile 的第二个语义是禁止指令重排序优化。普通的变量只能保证在方法的执行过程中，所有依赖赋值结果的地方都能正确获取结果，但不能保证变量赋值操作的顺序与程序代码中的顺序一致。

比如下面的代码，如果 initialized 没有被 volatile 修饰，那么由于指令重排序优化，赋值语句`initialized=true`有可能先于配置读取被执行，这就可能导致第二段代码执行出现问题。

```text
volatile boolean initialized = false;

// load config
//...
initialized = true


//下面代码在另一个线程中执行
while(!initialized){
    sleep();
}
doStuffWithConfig();
```

还有[双重检查的单例模式](../../software_engineering/design_pattern/singleton.md#lan-han-mo-shi-shuang-zhong-null-jian-cha)，如果静态变量没有被volatile修饰，也可能导致类似的问题。

> volatile 禁止重排序的语义在 JDK 1.5 才被修复，之前的版本就算使用 volatile 也不能完全避免指令重排序，即在 JDK 1.5 之前无法通过 DCL 方式实现单例模式。

## 如何实现可见性和禁止重排序？

在对volatile 修饰的变量进行修改后，会通过增加**内存屏障**（空操作指令加lock前缀）的方式，将本CPU缓存写入到内存中，这会导致其他CPU缓存失效，所以其他CPU再次访问这个值时只能重新从内存读取，这就保证了变量在不同线程之间的可见性。

内存屏障将数据同步到内存后，也就意味着内存屏障前的指令都是执行完成的，后面的指令不能再被重排序到内存屏障之前。





