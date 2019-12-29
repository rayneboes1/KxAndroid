# 单例模式

## 定义

保证某个类在全局只有一个实例存在。

## 代码实现

### 饿汉模式

```text
class Singleton {
    private static Singleton sInstance = new Singleton();

    //私有构造方法
    private Singleton() {
        
    }

    public static Singleton getInstance() {
        return sInstance;
    }
}
```

借助类加载机制避免了多线程重复创建实例问题，类加载时就进行初始化，没有实现懒加载。

### 懒汉模式（线程不安全）

```text
class Singleton {
    private static Singleton sInstance;

    //私有构造方法
    private Singleton() {
        
    }

    public static Singleton getInstance() {
        if (sInstance == null) {
            //多个线程进入判断会创建不同的实例
            sInstance = new Singleton();
        }
        return sInstance;
    }
}
```

实现了延迟初始化，但是多线程访问会重复创建实例。

### 懒汉模式（互斥锁）

```text
class Singleton {
    private static Singleton sInstance;

    //私有构造方法
    private Singleton() {
        
    }

    //使用 synchronized 保证互斥
    public static synchronized Singleton getInstance() {
        if (sInstance == null) {
            sInstance = new Singleton();
        }
        return sInstance;
    }
}
```

使用互斥锁避免的并发问题，但是由于每次获取实例都要加锁（大部分情况不需要这么做），导致性能较低。

### 懒汉模式（双重null检查）

```text
class Singleton {
    private static volatile Singleton sInstance;

    // 私有构造方法
    private Singleton() {

    }

    public static Singleton getInstance() {
        if (sInstance == null) {
            //如果多个线程进入判断，通过加锁保证互斥
            synchronized (Singleton.class) {
                if (sInstance == null) {
                    sInstance = new Singleton();
                }
            }
        }
        return sInstance;
    }
}
```

既避免了并发问题，又避免了互斥锁写法带来的性能问题。

如何保证可见性？

通过 [happens-before 原则](../../java/jvm/#happensbefore-yuan-ze)：对同一个锁的unlock happens before于对锁的lock。如果有多个线程进入了10行代码处，只有一个线程可以获得锁，如果该线程对sIntance复制的新对象，那么当下一个线程获取锁之后，sInstance 一定为非空。

### 静态内部类

```text
class Singleton {
    // 私有构造方法
    private Singleton() {
    }

    public static Singleton getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final Singleton INSTANCE = new Singleton();
    }
}
```

只有调用getInstance\(\)方法时才会加载静态内部类并创建静态常量，所以实现了延迟加载；通过类加载机制避免了多线程并发问题。

