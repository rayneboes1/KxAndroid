# 字节码指令

JVM 的指令是由一个字节长度的、代表某种特定操作含义的数字（操作码,Opcode）以及跟随其后的零至多个代表此操作所需参数（操作数,Operands）而构成。

由于JVM采用面向操作数栈而不是寄存器的架构，所以大多数指令都不包含操作数，只有一个操作码。

由于限制了操作码只有一个字节，所以操作码总数不会超过256个；又由于Class 文件格式放弃了编译后代码的操作数长度对齐，所以虚拟机在处理超过一个字节的数据时，需要在运行时从字节中重建出具体的数据结构\(字节拼接\)，这会导致执行字节码时损失一些性能；放弃操作数长度对齐的好处是可以省略很多填充和间隔符号，用一个字节代表操作数码，可以获得短小精干的编译代码，这种追求小数据量、高传输率的设计是由Java语言设计之初面向网络、智能家电的技术背景决定的。

## 字节码与数据类型

在 Java 虚拟机的指令集中，大多数指令都包括了其其操作所对应的数据类型信息，例如，iload 用于从局部变量表中加载int类型的数据到操作数栈中。但是由于操作码最多只能有256个，因此JVM 的指令集对于特定的操作只提供了有限的类型相关指令。

大多数指令都没支持byte、char、short，没有任何指令支持boolean。编译器会在编译期或运行期将byte和short类型的数据带符号扩展\(Sign-Extend\)为相应的int类型数据，将 boolean 和 char 类型零位扩展为相应的int类型数据，在处理这些类型的数组时，也会转换为使用对应的int类型的字节码指令处理。因此，**大多数对于boolean、byte、short和char类型的操作，实际上都是使用响应的int类型作为运算类型。**

## 字节码指令类型

### 加载和存储指令

加载和存储指令用于将数据在栈帧中的局部变量表和操作数栈之间来回传输。包括如下内容：

* 将局部变量加载到操作数栈：iload/iload\_&lt;n&gt;、fload/fload\_&lt;n&gt;、dload/dload\_&lt;n&gt;、aload/aload\_&lt;n&gt;
* 将一个数值从操作数栈存储到局部变量表:istore/istore\_&lt;n&gt;、fstore/fstore\_&lt;n&gt;、dstore/dstore\_&lt;n&gt;、astore/astore\_&lt;n&gt;
* 将一个常量加载到操作数栈：bipush、sipush、ldc、ldc\_w、ldc2\_w、aconst\_null、iconst\_m1、iconst\_&lt;i&gt;、lconst\_&lt;l&gt;、fconst\_&lt;f&gt;、dconst\_&lt;d&gt;
* 扩充局部变量表的访问索引：wide

以尖括号结尾的是带有操作数的通用指令的特殊形式，它们把操作数包含在了指令中，不需要进行取操作数的动作。

### 运算指令

运算指令用于对两个操作数栈上的值进行特定运算，并把结果存入操作数栈顶。对于byte、short、char和boolean类型，使用操作 int 类型的指令代替。

* 加法指令：iadd/ladd/fadd/dadd
* 减法指令：isub/lsub/fsub/dsub
* 乘法指令：imul/lmul/fmul/dmul
* 除法指令：idiv/ldiv/fdiv/ddiv
* 求余指令：irem/lrem/frem/drem
* 取反指令：ineg/lneg/fneg/dneg
* 位移指令：ishl/ishr/iushr/lshl/lshr/lushr
* 按位或：ior/lor
* 按位与：iand/land
* 按位异或：ixor/lxor
* 局部变量自增：iinc
* 比较指令：dcmpg/dcmpl/fcmpg/fcmpl/lcmp

\*\*\*\*

