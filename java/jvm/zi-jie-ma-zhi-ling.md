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

### **类型转换指令**

用于将两种不同的数值类型相互转换，一般用于实现代码中的显示类型转换或者处理字节码指令集中数据类型相关指令无法与数据类型一一对应的情况。

Java 虚拟机直接支持下面的的宽化类型转换\(Widening Numeric Conversions\)：

* int -&gt;long/float/double
* long-&gt;float/double
* float-&gt;double

处理窄化类型转换\(Narrowing Numeric Conversions\)时，必须显示的使用类型转换指令完成，转换指令包括：i2b/i2c/i2s/l2i/f2i/f2l/d2i/d2l/d2f。

窄化类型转换可能会导致转换结果产生不同的正负号、不同的数量级情况，转换过程很可能导致数值的精度丢失。

### 对象创建与访问指令

JVM 对类实例和数组的创建采用了不同的字节码指令：

* 创建类实例：new
* 创建数组指令：newarray/anewarray/multianewarray
* 访问类字段和实例字段：getfield/putfield/getstatic/putstatic
* 把一个数组元素加载到操作数栈：baload/caload/saload/iaload/laload/faload/daload/aaload
* 把操作数栈的值存储到数组元素中：bastore/castore/sastore/iastore/lastore/fastore/dastore/astore
* 取数组长度指令：arraylength
* 检查类实例类型：instanceof/checkcast

### 操作数栈管理指令

用于直接操作操作数栈。

* 将操作数栈的一个或两个元素出栈：pop/pop2
* 复制栈顶一个或两个数值并将复制值或双份的复制值重新压入栈顶：dup/dup2/dup\_x1/dup2\_x1/dup\_x2/dup2\_x2
* 将栈最顶端的两个数值互换：swap

### 控制转移指令

控制转移指令可以让Java虚拟机有条件或无条件地从指定的位置指令（而不是控制转移指令的下一条指令）继续执行程序。

> 从概念模型上理解，可以任务控制转移指令就是在有条件或无条件的修改PC寄存器的值。

* 条件分支：ifeq/iflt/ifle/ifne/ifgt/ifge/ifnull/ifnonnull/if\_icmpeq/if\_icmpne/if\_icmplt/if\_icmpgt/if\_icmple/if\_cmpge/if\_acmpeq/if\_acmpne
* 符合条件分支：tableswitch/lookupswitch
* 无条件分支：goto/goto\_w/jsr/jsr\_w/ret

对于 boolean/byte/char/short 类型的条件分支比较操作，都是用int类型的比较指令完成；对于long/float/double类型的条件分支比较操作，则会先执行对应类型的比较运算指令，运算指令会返回一个整型值到操作数栈中，随后在执行int类型的条件分支比较操作来完成整个分支跳转。

### 方法调用和返回指令

* invokevirtual：调用对象的实例方法，根据实例的类型进行分派（多态）
* invokeinterface：调用接口的方法，会在运行时搜索一个实现了这个接口方法的对象，执行对应的方法
* invokespecial：用于调用一些需要特殊处理的实例方法，包括实例初始化方法、私有方法和父类方法
* invokestatic：调用静态方法
* invokedynamic:用于在运行时动态解析出调用点限定符所引用的方法，并执行该方法。invokedynamic 的分派逻辑是由用户设定的引导方法决定的。
* 方法返回指令：ireturn/lreturn/freturn/dreturn/areturn

### 异常处理指令

显示抛出异常操作使用athrow指令实现，处理异常\(catch语句\)采用异常表完成。

### 同步指令

JVM 支持对方法和语句序列的同步，通过管程\(monitor\)完成。

方法级的同步是隐式的，即无需通过字节码指令控制，它实现在方法调用和返回操作中。虚拟机可以从方法表结构的ACC\_SYNCHRONIZED访问标志得知一个方法是否声明为同步方法。当方法调用时，如果这个标志被设置了，执行线程就要求先持有管程，才能执行方法，最后方法完成时会释放管程。

> 如果同步方法执行期间发生了异常且方法内部没有处理，则管程将在异常抛出到方法之外时释放。

同步指令序列通过 synchronized 关键字完成，JVM 提供了 monitorenter 和 monitorexit两条指令来支持 synchronized 的语义。

例如下面代码：

```text
void doWork(){
  synchronized(lock){
      doSomething();
}
}

void doSomething(){}
}
```

编译后的字节码如下：

```text
void doWork();
    descriptor: ()V
    flags:
    Code:
      stack=2, locals=3, args_size=1
         0: getstatic     #2                  // Field lock:Ljava/lang/Object;
         3: dup
         4: astore_1
         5: monitorenter
         6: aload_0
         7: invokevirtual #3                  // Method doSomething:()V
        10: aload_1
        11: monitorexit
        12: goto          20
        15: astore_2
        16: aload_1
        17: monitorexit
        18: aload_2
        19: athrow
        20: return

```

编译器必须确保无论方法是正常结束还是异常结束，调用的monitorenter的指令必须执行对应的monitorexit指令。

