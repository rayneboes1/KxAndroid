# Java 面试知识点

## 基础

### String、StringBuffer和StringBuilder的区别？

`String`/`StringBuffer`/`StringBuilder`最终底层存储与操作的都是 `char`数组。

但是 String 里面的 char 数组是用 final 修饰的，即不可变的，String 的大多数方法都是基于现有的 char 数组重新生成新的 char 数组，并赋值给新的 String 对象； 而 StringBuffer、StringBuilder 都继承了`AbstractStringBuilder`,底层的 char 数组是可以进行修改的，并且在容量不足时会自动进行扩容。相对来说，开销要小很多。

StringBuffer 是线程安全的，StringBuilder是线程不安全的，因为 StringBuffer 的方法通过加了 synchronized 加了互斥锁，而 StringBuilder 没有。  
如果对字符串进行频繁的增删时，优先使用 StringBuffer（多线程） 或 StringBuilder（单线程）。

### `String a=""`和`String b=new String("")`的的关系和异同？

String a ="",是在常量池创建了一个字符串常量，a 引用的是常量池中`""`的地址，而`new String("")`是基于`""`在堆中创建了一个新对象，b 引用的是新对象的地址，但新对象中存储的地址是常量池中`""`的地址。

因此，

```java
String a ="";
String b = new String("");
System.out.println(a==b); //false
System.out.println(a.equals(b));//true
String c = ""; //常量池中已有该值，直接返回引用
System.out.println(a==c); //true
String d = new String("");//新的对象
System.out.println(b==d);//false
```

> 1. String s = new String\(“hello”\)会创建2（1）个对象，String s = “hello”创建1（0）个对象。   
>
>    注：当字符串常量池中有对象hello时括号内成立！
>
> 2. 字符串如果是变量相加，先开空间，在拼接。
> 3. 字符串如果是常量相加，是先加，然后在常量池找，如果有就直接返回，否则，就创建。

### Object 的 equals\(\)和 == 的区别？

`==`比较的两个变量引用的内存地址是否一致，而`equals`方法是 `Object` 中声明的，默认实现也是比较引用地址。

```java
public boolean equals(Object obj) {
        return (this == obj);
    }
```

但Java中的所有对象都直接或间接的继承自 Object，因此可以通过重写`equals`方法来自定义比较的内容，比如`String`的`equals`方法就用于比较两个字符串内容是否相同。

### int 和 Integer 的区别？

int 是基本类型，Integer 是包装类型。

#### 为什么存在这两种类型呢？

在Java语言中，new一个对象存储在堆里，我们通过栈中的引用来使用这些对象；但是对于经常用到的一系列类型如int， 如果用new将其存储在堆里就不是很有效——特别是简单的小的变量。所以就出现了基本类型，同C++一样，Java采用了相似的做法， 对于这些类型不是用new关键字来创建，而是直接将变量的值存储在栈中，因此更加高效。

#### 有了基本类型为什么还要有包装类型呢？

我们知道Java是一个面相对象的编程语言，基本类型并不具有对象的性质， 为了让基本类型也具有对象的特征，就出现了包装类型 （如我们在使用集合类型Collection时就一定要使用包装类型而非基本类型）， 它相当于将基本类型“包装起来”，使得它具有了对象的性质，并且为其添加了属性和方法， 丰富了基本类型的操作。

### 装箱、拆箱什么含义？

> [https://www.cnblogs.com/dolphin0520/p/3780005.html](https://www.cnblogs.com/dolphin0520/p/3780005.html)

Java为每种基本数据类型都提供了对应的包装器类型，装箱就是自动将基本数据类型转换为包装器类型，拆箱就是自动将包装器类型转换为基本数据类型。

```java
Integer i = 10;//装箱

int j = i; //拆箱
```

装箱过程是通过调用包装器的 `valueOf` 方法实现的，而拆箱过程是通过调用包装器的 `xxxValue` 方法实现的。（xxx代表对应的基本数据类型）。

### 装箱面试点

```java
Integer a =100;
Integer b = 100;
Integer c = 200;
Integer d = 200;
System.out.println(a==b);//true
System.out.println(c==d);//false
```

因为 Integer 的 valueOf 方法会先在缓存中查找对象，而缓存中默认缓存了\[-128~127\]的值\(区间上限可以配置\)。  
其他包装类比如 Long/Short/Byte 也有类似实现。

而 Double 和 Float 是没有类似实现的，因为区间内的浮点数有无数个，无法进行缓存，因此

```java
Double d1 = 1.0;
Double d2 = 1.0;
System.out.println(d1==d2);//false
```

```java
Boolean b1 = true;
Boolead b2 = true;
System.out.println(b1==b2);//true
```

Boolean 包装类有两个常量 TRUE 和 FALSE，valueOf 的实现如下：

```java
public static Boolean valueOf(boolean b) {
        return (b ? TRUE : FALSE);
}
```

因此所有的 true 和 false 都是相等的。

**当 "=="运算符的两个操作数都是包装器类型的引用，则是比较指向的是否是同一个对象，而如果其中有一个操作数是表达式（即包含算术运算）则比较的是数值（即会触发自动拆箱的过程）。**

```java
Integer a = 1;
Integer b = 2;
Integer c = 3;
Integer d = 3;
Integer e = 321;
Integer f = 321;
Long g = 3L;
Long h = 2L;

System.out.println(c==d); //true
System.out.println(e==f); //false
System.out.println(c==(a+b)); //true
System.out.println(c.equals(a+b)); //true
System.out.println(g==(a+b)); //true
System.out.println(g.equals(a+b)); //false，类型不一致
System.out.println(g.equals(a+h)); //true,a+h 类型提升后为long
```

### 什么是内部类？

定义在类里面或者方法里面的类，称为内部类。内部类一般来说包括这四种：成员内部类、局部内部类、匿名内部类和静态内部类。

### 内部类作用

1. 每个内部类都能独立的继承一个接口的实现，所以无论外部类是否已经继承了某个\(接口的\)实现，对于内部类都没有影响。内部类使得多继承的解决方案变得完整。
2. 方便将存在一定逻辑关系的类组织在一起，又可以对外界隐藏。 

### 静态内部类和非静态内部类的区别？

1. 静态内部声明时要用`static`修饰，非静态内部类不需要。
2. 创建静态内部类的实例无需外部类的实例：

   ```java
   Outer.Inner i = new Outer.Inner();
   ```

   而创建非静态内部类实例时需要外部类实例：

   ```java
   Outer o = new Outer();
   Outer.Inner i = o.new Inner();
   ```

3. 非静态内部类持有外部类实例的引用，而静态内部类不会持有。

### 非静态内部类为什么可以访问外部类实例的属性？

因为非静态内部类默认持有外部类实例的引用。

### 为什么局部内部类和匿名内部类只能访问局部 final 变量？
内部类在使用外部方法传进的参数时，如果参数的值在编译期间就可以确定，则直接在匿名内部里面创建一个拷贝。如果局部变量的值无法在编译期间确定，则通过构造器传参的方式来对拷贝进行初始化赋值。而如果在内部类里对局部变量进行了修改，就回造成与外部变量数据不一致问题，令人费解，因此方法中的局部变量和形参都必须用final进行限定了。

> 内部类参考文章：https://www.cnblogs.com/dolphin0520/p/3811445.html   


### final、finally、finalize()分别表示什么含义？
- final 用于声明属性,方法和类, 分别表示属性不可变, 方法不可覆盖, 类不可继承，final+static 可以用来声明常量.
- finally 是异常处理语句结构的一部分，表示总是执行.
- finalize 是Object类的一个方法，在垃圾收集器执行的时候会调用被回收对象的此方法，可以覆盖此方法提供垃圾收集时的其他资源回收，例如关闭文件等. **JVM不保证此方法总被调用**.

### 重写和重载的区别？
重写(Override)是子类对父类的允许访问的方法的实现过程进行重新编写, 返回值和形参都不能改变。即外壳不变，核心重写。重写方法不能抛出新的检查异常或者比被重写方法申明更加宽泛的异常。  

重载(Overload) 是在一个类里面，方法名字相同，而参数不同。*返回类型可以相同也可以不同。*
每个重载的方法（或者构造函数）都必须有一个独一无二的参数类型列表。

### 接口和抽象类的区别
- 抽象类可以有构造方法,接口不行
- 抽象类可以有普通成员变量 接口没有
- 抽象类可以有非抽象的方法 接口必须全部抽象
- 抽象类可以有私有，接口必须全部public

### Object有哪些公用方法？
`getClass()` `hashCode()` `equals()`  `toString()` `notify()` `notifyAll()`   

`wait(long timeout)` `wait(long timeout, int nanos)` `wait()`

## 异常
### 遇见过哪些运行时异常？异常处理机制知道哪些？  

Java 中 Throwable 是所有异常和错误的基类，Throwable 子类有 Exception 和 Error。  

Exception 是所有异常的基类，应用可以对异常进行捕捉。  
Error 代表严重的错误发生，比如系统崩溃、虚拟机出错误，应用通常无法处理这些错误，也不应该捕捉。  

#### 受检异常和非受检异常
通常，Java的异常(包括Exception和Error)分为受检异常（checked exceptions）和非受检异常（unchecked exceptions）。
受检异常（编译器要求必须处置的异常）：正确的程序在运行中，很容易出现的、情理可容的异常状况。受检异常虽然是异常状况，但在一定程度上它的发生是可以预计的，而且一旦发生这种异常状况，就必须采取某种方式进行处理。除了RuntimeException及其子类以外，其他的 Exception 类及其子类都属于可查异常。这种异常的特点是Java编译器会检查它，也就是说，当程序中可能出现这类异常，要么用try-catch语句捕获它，要么用throws子句声明抛出它，否则编译不会通过。  

非受检异常(编译器不要求强制处置的异常):包括运行时异常（RuntimeException与其子类）和错误（Error）。

#### 运行时异常和非运行时异常
运行时异常都是`RuntimeException`类及其子类异常，如 `NullPointerException`、`IndexOutOfBoundsException`、`ClassCastException`、`IllegalArgumentException`、`IllegalStateException`等，这些异常是不检查异常，程序中可以选择捕获处理，也可以不处理。这些异常一般是由程序逻辑错误引起的，程序应该从逻辑角度尽可能避免这类异常的发生。

当出现RuntimeException的时候，我们可以不处理。当出现这样的异常时，总是由虚拟机接管。比如：我们从来没有人去处理过NullPointerException异常，它就是运行时异常，并且这种异常还是最常见的异常之一。 
出现运行时异常后，如果没有捕获处理这个异常（即没有catch），系统会把异常一直往上层抛，一直到最上层，如果是多线程就由Thread.run()抛出，如果是单线程就被main()抛出。抛出之后，如果是线程，这个线程也就退出了。如果是主程序抛出的异常，那么这整个程序也就退出了。运行时异常是Exception的子类，也有一般异常的特点，是可以被catch块处理的。只不过往往我们不对他处理罢了。也就是说，你如果不对运行时异常进行处理，那么出现运行时异常之后，要么是线程中止，要么是主程序终止。 
如果不想终止，则必须捕获所有的运行时异常，决不让这个处理线程退出。队列里面出现异常数据了，正常的处理应该是把异常数据舍弃，然后记录日志。不应该由于异常数据而影响下面对正常数据的处理。


非运行时异常是RuntimeException以外的异常，类型上都属于Exception类及其子类。如IOException、SQLException等以及用户自定义的Exception异常。对于这种异常，JAVA编译器强制要求我们必需对出现的这些异常进行catch并处理，否则程序就不能编译通过。

#### 异常处理

抛出异常，throw 或方法声明时使用throws声明可能的异常   

处理异常 try catch finally


### 遇见过哪些运行时异常？异常处理机制知道哪些？

## 反射

### 什么是反射，有什么作用和应用？
JAVA反射机制是在运行状态中, 对于任意一个类, 都能够知道这个类的所有属性和方法; 对于任意一个对象, 都能够调用它的任意一个方法和属性; 这种动态获取的信息以及动态调用对象的方法的功能称为java语言的反射机制.   

主要作用有三：
- 运行时取得类的方法和字段的相关信息。
- 创建某个类的新实例(.newInstance())
- 取得字段引用直接获取和设置对象字段，无论访问修饰符是什么。

**应用**    

ButterKnife 在 bindView 时，就是通过反射来加载Activity对应的binding类，并创建对应类的实例来给Activity的view赋值。  
