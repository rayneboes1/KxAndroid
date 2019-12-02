# Java 面试知识点

## 基础

### String、StringBuffer和StringBuilder的区别？

`String`/`StringBuffer`/`StringBuilder`最终底层存储与操作的都是 `char`数组。  

但是 String 里面的 char 数组是用 final 修饰的，即不可变的，String 的大多数方法都是基于现有的 char 数组重新生成新的 char 数组，并赋值给新的 String 对象；
而 StringBuffer、StringBuilder 都继承了`AbstractStringBuilder`,底层的 char 数组是可以进行修改的，并且在容量不足时会自动进行扩容。相对来说，开销要小很多。

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
>1. String s = new String(“hello”)会创建2（1）个对象，String s = “hello”创建1（0）个对象。   
 注：当字符串常量池中有对象hello时括号内成立！
> 2. 字符串如果是变量相加，先开空间，在拼接。
> 3. 字符串如果是常量相加，是先加，然后在常量池找，如果有就直接返回，否则，就创建。  

### Object 的 equals()和 == 的区别？
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
在Java语言中，new一个对象存储在堆里，我们通过栈中的引用来使用这些对象；但是对于经常用到的一系列类型如int，
如果用new将其存储在堆里就不是很有效——特别是简单的小的变量。所以就出现了基本类型，同C++一样，Java采用了相似的做法，
对于这些类型不是用new关键字来创建，而是直接将变量的值存储在栈中，因此更加高效。

#### 有了基本类型为什么还要有包装类型呢？
我们知道Java是一个面相对象的编程语言，基本类型并不具有对象的性质，
为了让基本类型也具有对象的特征，就出现了包装类型
（如我们在使用集合类型Collection时就一定要使用包装类型而非基本类型），
它相当于将基本类型“包装起来”，使得它具有了对象的性质，并且为其添加了属性和方法，
丰富了基本类型的操作。

### 装箱、拆箱什么含义？
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
   
因为 Integer 的 valueOf 方法会先在缓存中查找对象，而缓存中默认缓存了[-128~127]的值(区间上限可以配置)。  
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











