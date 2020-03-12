# Java

## Java 技术体系

![Java &#x6280;&#x672F;&#x4F53;&#x7CFB;&#x67B6;&#x6784;&#x56FE;](../.gitbook/assets/image%20%2839%29.png)

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

### 前身

* 1991 年 4 月，Java 的前身 Oak 诞生，目的是能够在各种消费电子产品上运行的程序架构。
* 1995 年 5 月 23 日，Oak 改名为 Java，并在 SunWorld 大会上正式发布1.0版本，Java 语言第一次提出了“Write Once，Run Everywhere”

### JDK 1.0

* 1996 年 1 月 23 日，JDK 1.0 发布。JDK 1.0 提供了一个纯解释执行的 Java 虚拟机实现（Sun Classic VM）。JDK 1.0 版本的代表技术包括：Java 虚拟机、AWT、Applet等。

### JDK 1.1

1997年 2 月 19 日，Sun 公司发布了 JDK 1.1，代表技术：JAR 文件格式、JDBC、JavaBeans、RMI。Java 语法上有内部类、反射。

### JDK 1.2

* 1998 年 12 月 4 日，JDK 1.2 发布。Sun 在这个版本中将Java技术体系拆分成三个方向：J2SE、J2EE 和 J2ME。代表性技术很多：EJB、Java Plug-in、Java IDL、Swing，在这个版本中 JVM 第一次内置了 JIT 编译器。
* 1999 年 4 月 27 日，HotSpot 虚拟机发布。是 JDK 1.3及之后所有版本的Sun JDK 的默认虚拟机。

### JDK 1.3

* 2000年5月8日，JDK 1.3  发布。对类库进行了改进（数学运算和新的 Timer API），JNDI 服务从JDK 1.3开始作为平台级服务提供，使用CORBA IIOP 来实现 RMI 的通信协议。这个版本对 Java 2D 做了很多改进。

### JDK 1.4

* 2002 年 2 月 13 日，JDK 1.4 发布。JDK 1.4 是 Java 真正走向成熟的版本。新增技术特性：正则表达式、异常链、NIO、日志类、XML 解析器和XSLT转换器等

### JDK 1.5

* 2004年9月30日，JDK 1.5 发布，在Java 语法易用性上做了很大改进：自动装箱、泛型、自动注解、枚举、可变长参数、foreach 循环等；这个版本还改进了 Java 内存模型、提供了 java.util.concurrent 并发包等。

### JDK 1.6

* 2006年 12 月 11日，JDK 1.6 发布。这个版本终结了 J2SE、J2EE 和 J2ME 的命名法，启动 Java SE 6、Java ME 6、Java EE  6的命名方式。改进：提供动态语言支持（通过内置Mozilla JavaScript Rhino 引擎实现）、提供编译 API和微型 HTTP 服务器 API；对 JVM 内部做了很大改进：锁与同步、垃圾收集、类加载等方面的算法都有很大改动。
* 2006 年 11 月 13 日，Sun 公司宣布会将 Java 开源，并在随后一年多的时间里，陆续将 JDK  的各个部分在 GPL v2 协议下开源，并建立 OpenJDK 组织对这些源码进行独立管理。
* 2009 年 4 月 20 日，Oracle 收购 Sun 公司

### JDK 1.7

* 2011 年 7月28日，JDK  1.7 发布，提供新的 G1 收集器、加强对非 Java 语言的调用支持、升级类加载架构等。从 Java SE 7 update 4 开始， Java 开始支持 Mac os 系统。

### JDK 1.8 

### JDK 1.9 

### JDK 10 

### JDK 11 

### JDK 12

## JVM 发展史

### Sun Classic/Exact VM

Classic 是 JDK 1.0 中所带的虚拟机，它只能用纯解释器的方式来执行代码。如果要使用 JIT 则需要使用外挂，但是如果外挂了JIT编译器，JIT 编译器就完全接管了虚拟机的执行系统，解释器便不再工作了。执行效率很慢。

JDK 1.2 时曾在 Solaris 平台发布过一款名为 Exact VM 的虚拟机，它的执行系统已经具备现代高性能虚拟机的雏形：如两级即时编译器、编译器与解释器混合工作模式等。

Exact VM 因使用准确式内存管理而得名，即虚拟机知道内存中某个数据具体是什么类型，这样可能在GC的时候准确判断堆上的数据是否还可能被使用。

### Sun HotSpot VM

由 Longview Technologies 开发，Sun 1997 年收购了该公司，因为注意到这款虚拟机在 JIT 编译上有许多优秀的理念和实际效果。Hotspot 就是指的热点代码探测技术，Hotspot VM 的热点代码探测能力可以通过执行计数器找出最具有编译价值的代码，然后通知 JIT 编译器以方法为单位进行编译。如果一个方法被频繁调用，或方法中有效循环次数很多，将 分别会触发标准编译和OSR（栈上替换）编译动作。通过编译器与解释器恰当的协同工作，可以在最优化的程序响应时间与最佳执行性能中取得平衡，而且无需等待本地代码输出才能执行程序，即编译的时间压力也相对减小，这样有助于引入更多的代码优化技术，输出质量更高的本地代码。

### Sun Mobile-Embedded VM/Meta-Circular VM

#### KVM

K 是 "kilobyte" 的意思，它强调简单、轻量、高度可移植，但是运行速度比较慢。在 Android、iOS 等智能手机操作系统出现前曾经在手机平台上得到广泛的应用。

#### CDC/CLDC HotSpot Implementation

CDC/CLDC 是 Connected \(Limited\) Device Configuration,在 JSR-139/JSR-218 规范中进行定义，它希望在手机、电子书、PDA 等设备上建立统一的 Java 编程接口 而 CDC-HI VM和 CLDC-HI VM 则是它们的一组参考实现。

#### Squawk VM

由 Sun 公司开发，运行于 Sun SPOT（一种手持的wifi设备），也曾经运用于 Java Card，这是一个 Java 代码比重很高的嵌入式虚拟机实现，其中诸如类加载器、字节码验证器、垃圾收集器、解释器、编译器和线程调度都是Java语言本身完成的，仅靠C语言来编写设备I/O和必要的本地代码。

#### JavaInJava

Sun 公司于1997-1998年研发的一个实验性质的虚拟机，从名字可以看出，它试图以 Java 语言来实现 Java 语言本身的运行环境，即所谓的「元循环」。它必须运行在另外一个宿主虚拟机上，内部没有JIT编译器，代码只能以解释模式执行。

#### Maxine VM

和 JavaInJava 相似，它也是一个几乎全部以Java代码（只有用于启动 JVM 的加载器使用 C 语言编写）实现的元循环Java虚拟机。这个项目2005年开始，现在仍然在发展中，比 JavaInJava「靠谱」很多，它有先进的 JIT 编译器和垃圾收集器（但没有解释器），可在宿主模式或独立模式下执行，其执行效率已经接近了 HotSpot Client VM 的水平。

### BEA JRockit/IBM J9 VM

JRockit VM 曾经号称「世界上速度最快的虚拟机」，它是BEA 公司2002年从 Appeal Virtual Machines 公司收购的虚拟机，BEA 公司将其发展为一款专门为服务器硬件和服务器端应用场景高度优化的虚拟机，由于专注服务端应用，它可以不太关注应用启动速度，因此内部不包含解析器实现，全部代码都靠即时编译器编译后执行。

IBM J9 VM 最初是由 IBM Ottawa 实验室一个名为 SmallTalk 的虚拟机扩展而来。IBM J9 的市场定位与 Sun HotSpot 比较接近，它是一款设计上从服务端到桌面应用再到嵌入式都全面考虑的多用途虚拟机。J9的开发目的是作为 IBM 各种 Java 产品的执行平台。

### Azul VM /BEA Liquid VM

Azul VM 是 Azul Systems 公司在 HotSpot 基础上进项大量改进，运行于 Azul Systems 公司的专有硬件 Vega 系统上的 Java 虚拟机，每个 Azul VM 实例都可以管理至少数十个CPU和数百GB内存的硬件资源，并提供在巨大内存范围内实现可控的 GC 时间的垃圾收集器、为专有硬件优化的线程调度等优秀特性。

Liquid VM 即是现在的 JRockit VE（Virtual Edition），它是 BEA 公司开发的，可以直接运行在自家的 Hypervisor 系统上的 JRockit VM 的虚拟化版本。Liquid VM 不需要操作系统的支持，或者说它本身实现了一个专用操作系统的必要功能，如文件系统、网络支持等。

### Apache Harmony/Google Android Dalvik VM

Apache Harmony 是一个 Apache 软件基金会旗下以 Apache License 协议开源的实际兼容于 JDK 1.5 和 JDK 1.6 的 Java 程序运行平台，它包含自己的虚拟机和 Java 库，但是没有通过  TCK \(Technology Compatibility Kit\)认证。Apache Harmony 没有经过大规模的商业运用，但是它的许多代码被吸纳进 IBM 的 JDK 7 实现以及 Google  Android SDK 中，对 Android 的 发展起到了很大的推动作用。

Dalvik VM 是 Android  平台的核心组成部分之一，它并不是一个 Java 虚拟机，没有遵循 Java 虚拟机规范，不能直接执行 Java 的 Class 文件，使用的寄存器架构而不是 JVM 中常见的栈架构。但是它执行的 dex \(Dalvik Executable\)文件可以通过 class 文件转化而来，使用 Java 语法编写应用程序，可以直接使用大部分的 Java API 等。

###  Microsoft JVM

微软为了在 IE 3中支持 Java Applets 应用而开发的自己的 Java 虚拟机，虽然只有Windows版本，却是当时 Windows 下性能最好的 Java 虚拟机，但 1997 年 10 月，Sun 公司正式以侵占商标 、不正当竞争等罪名控告微软公司，最终微软承诺终止其 Java 虚拟机的发展，并逐步在产品中移除 Java 虚拟机相关功能。

## 相关链接

* [《深入理解 Java 虚拟机》第一章：走进 Java](https://item.jd.com/12607299.html)
* [图片来源](https://docs.oracle.com/javase/8/docs/)

