# Java

## Java 技术体系

![Java &#x6280;&#x672F;&#x4F53;&#x7CFB;&#x67B6;&#x6784;&#x56FE;](../.gitbook/assets/image%20%2837%29.png)

传统意义上讲，Sun 官方定义的Java技术体系分为以下几个部分：

* Java 程序设计语言
* 各种硬件平台上的 Java 虚拟机
* Class 文件格式
* Java API 类库
* 三方 Java 类库

### JDK 与 JRE

可以把 Java 语言、Java 虚拟机和 Java API 类库称为 JDK\(Java Development Kit\)，JDK 是用于支持程序开发的最小环境；把 Java API 类库里的 Java SE API 子集和 Java 虚拟机这两部分称为 JRE\(Java Runtime Environment\)，JRE 是支持 Java 程序运行的标准环境。

### 技术平台划分

按照技术服务的领域划分，可以分为 4 个平台：

* Java Card：支持Java小程序 \(Applets\) 运行在小内存设备上的平台
* Java ME\(Micro  Edition\)：支持Java 程序运行在移动终端上的平台，对 Java API 有所精简，并加入了针对移动端的支持（J2ME）
* Java SE\(Standard Edition\)：支持面向桌面级应用的平台，提供了完成了 Java 核心 API\(J2SE\)
* Java EE\(Enterprise Edition\)：支持使用多层架构的企业应用（ERP/CRM）的 Java 平台，除了提供 Java SE API 外，还对其做了大量的扩充并提供了相关的部署支持\(J2EE\) 

## Java 发展史

* 1991 年 4 月，Java 的前身 Oak 诞生，目的是能够在各种消费电子产品上运行的程序架构。
* 1995 年 5 月 23 日，Oak 改名为 Java，并在 SunWorld 大会上正式发布1.0版本，Java 语言第一次提出了“Write Once，Run Everywhere”
* 1996 年 1 月 23 日，JDK 1.0 发布。JDK 1.0 提供了一个纯解释执行的 Java 虚拟机实现（Sun Classic VM）。JDK 1.0 版本的代表技术包括：Java 虚拟机、AWT、Applet等。
* 1997年 2 月 19 日，Sun 公司发布了 JDK 1.1，代表技术：JAR 文件格式、JDBC、JavaBeans、RMI。Java 语法上有内部类、反射。
* 1998 年 12 月 4 日，JDK 1.2 发布。Sun 在这个版本中将Java技术体系拆分成三个方向：J2SE、J2EE 和 J2ME。代表性技术很多：EJB、Java Plug-in、Java IDL、Swing，在这个版本中 JVM 第一次内置了 JIT 编译器。
* 1999 年 4 月 27 日，HotSpot 虚拟机发布。是 JDK 1.3及之后所有版本的Sun JDK 的默认虚拟机。
* 2000年5月8日，JDK 1.3  发布。对类库进行了改进（数学运算和新的 Timer API），JNDI 服务从JDK 1.3开始作为平台级服务提供，使用CORBA IIOP 来实现 RMI 的通信协议。这个版本对 Java 2D 做了很多改进。
* 2002 年 2 月 13 日，JDK 1.4 发布。JDK 1.4 是 Java 真正走向成熟的版本。新增技术特性：正则表达式、异常链、NIO、日志类、XML 解析器和XSLT转换器等。
* 2004年9月30日，JDK 1.5 发布，在Java 语法易用性上做了很大改进：自动装箱、泛型、自动注解、枚举、可变长参数、foreach 循环等；这个版本还改进了 Java 内存模型、提供了 java.util.concurrent 并发包等。
* 2006年 12 月 11日，JDK 1.6 发布。这个版本终结了 J2SE、J2EE 和 J2ME 的命名法，启动 Java SE 6、Java ME 6、Java EE  6的命名方式。改进：提供动态语言支持（通过内置Mozilla JavaScript Rhino 引擎实现）、提供编译 API和微型 HTTP 服务器 API；对 JVM 内部做了很大改进：锁与同步、垃圾收集、类加载等方面的算法都有很大改动。
* 2006 年 11 月 13 日，Sun 公司宣布会将 Java 开源，并在随后一年多的时间里，陆续将 JDK  的各个部分在 GPL v2 协议下开源，并建立 OpenJDK 组织对这些源码进行独立管理。
* 2009 年 4 月 20 日，Oracle 收购 Sun 公司，
* 2011 年 7月28日，JDK  1.7 发布，提供新的 G1 收集器、加强对非 Java 语言的调用支持、升级类加载架构等。从 Java SE 7 update 4 开始， Java 开始支持 Mac os 系统。
* todo JDK 1.8 1.9 10 11 12

## JVM 发展史

### Sun Classic/Exact VM

Classic 是 JDK 1.0 中所带的虚拟机，它只能用纯解释器的方式来执行代码。如果要使用 JIT 则需要使用外挂，但是如果外挂了JIT编译器，JIT 编译器就完全接管了虚拟机的执行系统，解释器便不再工作了。执行效率很慢。

JDK 1.2 时曾在 Solaris 平台发布过一款名为 Exact VM 的虚拟机，它的执行系统已经具备现代高性能虚拟机的雏形：如两级即时编译器、编译器与解释器混合工作模式等。

Exact VM 因使用准确式内存管理而得名，即虚拟机知道内存中某个数据具体是什么类型。





## 相关链接

* [《深入理解 Java 虚拟机》第一章：走进 Java](https://item.jd.com/12607299.html)
* [图片来源](https://docs.oracle.com/javase/8/docs/)

