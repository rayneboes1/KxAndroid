# 类加载机制

虚拟机把描述类的数据从Class文件加载到内存，并对数据进行校验、解析和初始化，最终形成可以被虚拟机使用的 Java 类型，这就是虚拟机的类加载机制。

## 类的「生命周期」

![](../../.gitbook/assets/jie-ping-2019121122.24.23.png)

## 类加载的时机

对于类加载过程开始执行「加载」的时机，JVM 规范并没有强制约束，但是对于类的「初始化」阶段，虚拟机规范则严格规定了有且只有5种情况必须立即对类进行初始化（加载、验证等过程自然需要在此之前开始）：

### 主动引用

* 遇到 new/getstatic/putstatic/invokestatic 这4条字节码指令时，Java 代码场景：使用`new`实例化一个类时、读取或设置一个类的静态字段（被final修饰、已在编译期把结果放入常量池的静态字段除外）、调用一个类的静态方法。
* 使用 java.lang.reflect 包的方法对类进行反射调用时
* 初始化一个类时，如果它的父类未被初始化，则需要先初始化父类
* 虚拟机启动时，用户要指定一个要执行的主类\(包含main方法\)，虚拟机会先初始化这个类
* 当使用JDK 1.7 的动态语言支持时，如果一个 java.lang.invoke.MethodHandle 实例最后的解析结果REF\_getStatic、REF\_putStatic、REF\_invokeStatic 的方法句柄对应的类没有进行过初始化

以上的情况为「主动引用」，对于其他「被动引用」的情况不会触发类的初始化。

### 被动引用

典型的「被动引用」场景：

#### 通过子类引用父类的静态字段，不会导致子类初始化

```text
public class SuperClass{
    public static int value = 123;
}

public class SubClass extends SuperClass{

}

public class TestClass{
    public static void main(String... args){
        //只会触发父类的初始化
        System.out.println(SubClass.value);
    }

}
```

#### 通过数组定义来引用类

```text
public class TestClass{
    public static void main(String... args){
       SuperClass[] arr = new SuperClass[10];
    }

}
```

不会触发 SuperClass 的初始化，但是会触发一个名为"\[Lcom.test.SuperClass"的类的初始化，这个类时虚拟机自动生成的，直接继承自java.lang.Object，创建动作有字节码指令 newarray 触发。

#### 引用已经存在类的常量池中的常量

```text
public class ConstClass{
    public static final String CONST = "hello world";
}


public class TestClass{
    public static void main(String... args){
        //不会触发 ConstClass 的初始化
        System.out.println(ConstClass.CONST);
    }
}
```

虽然在 Java 源码中引用了 ConstClass 的常量，但是在编译阶段存在常量传播优化，已经将此常量的值存储到了 TestClass 类的常量池中，因此此处引用实际是对 TestClass 常量池中常量的引用。也就是说，TestClass 的 Class 文件之中并没有ConstClass 的引用符号，两个类在编译后就没有关系了。

对于接口的加载，编译器会为其生成&lt;clinit&gt;类构造器用于初始化接口中定义的成员变量。与类初始化不同的是，接口在出初始化时并不要求其父接口都完成了初始化，只有在真正使用到父接口时\(比如使用父接口的常量\)才回初始化。

## 类加载过程

#### 加载

1. 通过类的全限定名来获取定义此类的二进制字节流
2. 将这个字节流代表的静态存储结构转化方法区的运行时数据结构
3. 在内存中生成一个代表这个类的 java.lang.Class 对象，作为方法区这个类的各种数据的访问入口

#### 验证

连接的第一步，目的是确保Class文件字节流中包含的信息符合当前虚拟机要求，并且不会危害 虚拟机自身的安全。

* 文件格式验正
* 元数据验正
* 字节码验证
* 符号引用 验证

#### 准备

正式为类变量分配内存并设置变量初始值的阶段，这些变量使用的初始值都将在方法区分配。进行内存分配的是类变量而不是实例变量，会赋值为初始值。只有被final修饰的常量才会被赋成设定的值。

#### 解析

将常量池中的符号引用替换为直接引用过程。

* 类或接口解析
* 字段解析
* 类方法解析
* 接口方法解析

#### 初始化 

真正开始执行类中定义的 Java 程序代码，根据程序员制定的主观计划去初始化类变量和其他资源。

## 类加载器的Parents Delegation 模型（TODO）

![](../../.gitbook/assets/jie-ping-2019121122.42.48.png)

双亲委派模型除了顶层的启动类加载器外，所有的类加载器都有自己的父类加载器。

工作过程：类加载器收到类加载请求后，首先会委派给父类加载器进行加载，每一层都是如此，最终类加载请求会传给顶层的启动类加载器。只有当父类加载器无法完成加载请求时，子类加载器才会尝试自己去加载。

使用双亲委派模型组织类加载器，**使得类同加载器一起具备带有优先级的层次关系。例如java.lang.Object 只会委派给启动类加载器进行加载，防止出现多份同样的字节码以及核心类被篡改。**

