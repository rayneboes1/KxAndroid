# 基础

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
> 2. 字符串如果是变量相加，先开空间，再拼接。（实际上是通过StringBuilder）
> 3. 字符串如果是常量相加，是先加，然后在常量池找，如果有就直接返回，否则，就创建。

### 

## 基本类型与包装类

### int 和 Integer 的区别？

int 是基本类型，Integer 是包装类型。

#### 为什么存在这两种类型呢？

在Java语言中，new一个对象存储在堆里，我们通过栈中的引用来使用这些对象；但是对于经常用到的一系列类型如int， 如果用new将其存储在堆里就不是很有效——特别是简单的小的变量。所以就出现了基本类型，同C++一样，Java采用了相似的做法， 对于这些类型不是用new关键字来创建，而是直接将变量的值存储在栈中，因此更加高效。

#### 有了基本类型为什么还要有包装类型呢？

我们知道Java是一个面相对象的编程语言，**基本类型并不具有对象的性质**， 为了让基本类型也具有对象的特征，就出现了包装类型 （如我们在使用集合类型Collection时就一定要使用包装类型而非基本类型）， 它相当于将基本类型“包装起来”，使得它具有了对象的性质，并且为其添加了属性和方法， 丰富了基本类型的操作。

### 装箱、拆箱什么含义？

> [https://www.cnblogs.com/dolphin0520/p/3780005.html](https://www.cnblogs.com/dolphin0520/p/3780005.html)

Java为每种基本数据类型都提供了对应的包装器类型，装箱就是自动将基本数据类型转换为包装器类型，拆箱就是自动将包装器类型转换为基本数据类型。

```java
Integer i = 10;//装箱

int j = i; //拆箱
```

装箱过程是通过调用包装器的 `valueOf` 方法实现的，而拆箱过程是通过调用包装器的 `xxxValue` 方法实现的。（xxx代表对应的基本数据类型）。

### 装箱拆箱面试点

```java
Integer a =100;
Integer b = 100;
Integer c = 200;
Integer d = 200;
System.out.println(a==b);//true
System.out.println(c==d);//false
```

**因为 Integer 的 valueOf 方法会先在缓存中查找对象，而缓存中默认缓存了\[-128~127\]的值\(区间上限可以配置\)，**其他包装类比如 Long/Short/Byte 也有类似实现，但是 Double 和 Float 是没有类似实现的，因为区间内的浮点数有无数个，无法进行缓存，因此

```java
Double d1 = 1.0;
Double d2 = 1.0;
System.out.println(d1==d2);//false
```

Boolean 包装类有两个常量 TRUE 和 FALSE，valueOf 的实现如下：

```java
public static Boolean valueOf(boolean b) {
        return (b ? TRUE : FALSE);
}
```

因此所有的 true 和 false 都是相等的。

```java
Boolean b1 = true;
Boolead b2 = true;
System.out.println(b1==b2);//true
```

#### **比较**

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

## 类型提升规则

  Java定义了若干使用于表达式的类型提升规则： 

1. 所有的byte型. short型和char型将被提升到int型\(例外: final修饰的short, char变量相加后不会被自动提升。\)
2. 如果一个操作数是long形 计算结果就是long型;
3. 如果一个操作数是float型，计算结果就是float型;
4. 如果一个操作数是double型，计算结果就是double型;

   另一种归纳方式\(《Java核心技术卷I》P43\): 　　

1. 如果两个操作数其中有一个是double类型，另一个操作就会转换为double类型
2. 否则，如果其中一个操作数是float类型，另一个将会转换为float类型
3. 否则，如果其中一个操作数是long类型，另一个会转换为long类型。
4. 否则，两个操作数都转换为int类型。

[java 基本数据类型及自动类型提升](https://www.cnblogs.com/skipping/p/5449019.html)

## 内部类

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

3. **非静态内部类持有外部类实例的引用，而静态内部类不会持有。**

### 非静态内部类为什么可以访问外部类实例的属性？

因为非静态内部类默认持有外部类实例的引用。

### 为什么局部内部类和匿名内部类只能访问局部 final 变量？

内部类在使用外部方法传进的参数时，如果参数的值在编译期间就可以确定，则直接在匿名内部里面创建一个拷贝。如果局部变量的值无法在编译期间确定，则通过构造器传参的方式来对拷贝进行初始化赋值。而如果在内部类里对局部变量进行了修改，就回造成与外部变量数据不一致问题，令人费解，因此方法中的局部变量和形参都必须用final进行限定了。

> 内部类参考文章：[https://www.cnblogs.com/dolphin0520/p/3811445.html](https://www.cnblogs.com/dolphin0520/p/3811445.html)

### final、finally、finalize\(\)分别表示什么含义？

* final 用于声明属性,方法和类, 分别表示属性不可变, 方法不可覆盖, 类不可继承，final+static 可以用来声明常量.
* finally 是异常处理语句结构的一部分，表示总是执行.
* finalize 是Object类的一个方法，在垃圾收集器执行的时候会调用被回收对象的此方法，可以覆盖此方法提供垃圾收集时的其他资源回收，例如关闭文件等. **JVM不保证此方法总被调用**.

### 重写和重载的区别？

重写\(Override\)是子类对父类的允许访问的方法的实现过程进行重新编写, 返回值和形参都不能改变。即外壳不变，核心重写。重写方法不能抛出新的检查异常或者比被重写方法申明更加宽泛的异常。

重载\(Overload\) 是在一个类里面，方法名字相同，而参数不同。_返回类型可以相同也可以不同。_ 每个重载的方法（或者构造函数）都必须有一个独一无二的参数类型列表。

### 接口和抽象类的区别

* 抽象类可以有构造方法,接口不行
* 抽象类可以有普通成员变量 接口没有
* 抽象类可以有非抽象的方法 接口必须全部抽象
* 抽象类可以有私有，接口必须全部public
* 抽象类更强调模版和结构，接口更偏向功能补充

## Object

### Object有哪些公用方法？

`getClass()` `hashCode()` `equals()` `toString()` `notify()` `notifyAll()`

`wait(long timeout)` `wait(long timeout, int nanos)` `wait()`

### hashcode\(\)的作用，与equal\(\)有什么区别？

 [面试官爱问的equals与hashCode](https://juejin.im/post/5a4379d4f265da432003874c)

### equals\(\)和 == 的区别？

`==`比较的两个变量引用的内存地址是否一致，而`equals`方法是 `Object` 中声明的，默认实现也是比较引用地址。

```java
public boolean equals(Object obj) {
        return (this == obj);
    }
```

但Java中的所有对象都直接或间接的继承自 Object，因此可以通过重写`equals`方法来自定义比较的内容，比如`String`的`equals`方法就用于比较两个字符串内容是否相同。

## 深拷贝与浅拷贝

浅拷贝：拷贝对象的时候，只对基本数据类型进行了拷贝，而对引用数据类型只是进行了引用的传递，而没有真实的创建一个新的对象。

深拷贝：复制对象时，对引用数据类型进行拷贝的时候，创建了一个新的对象，并且复制其内的成员变量。

Java 中Object类有一个clone方法，但是类想要支持 clone 操作需要实现 Cloneable 接口。Object 的 clone 方法默认实现的浅拷贝。

实现深拷贝需要对引用类型的变量调用对应的clone 方法，进行递归clone，直到只包含基本类型为止。





