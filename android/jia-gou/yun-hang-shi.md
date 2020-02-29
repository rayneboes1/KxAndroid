# 运行时

[dalvik 与 art](https://source.android.com/devices/tech/dalvik)

## 发展史

* 4.4之前，Dalvik-及时编译（JIT）：安装快、每次启动应用都需要编译，耗电量大
* 4.4 以后，ART（AOT）预先编译：安装时将dex编译好，以后每次运行不用再编译。运行快、省电，安装慢。
* 7 以后：JIT+AOT，安装时不编译，而是通过JIT运行；常用代码会被记录，生成profile文件；当设备充电或闲置时，再通过扫描 profile 文件，通过AOT对热点代码进行编译。结合了JIT和AOT的优点

## JIT 编译技术

jit 相当于是给代码加了缓存，对于频繁运行的代码（hot code），将编译结果保存起来，下次执行时不用在做解释或编译，直接运行机器码即可。

[深入浅出 JIT 编译器](https://www.ibm.com/developerworks/cn/java/j-lo-just-in-time/index.html)

## AOT 编译技术

在应用安装时就编译成机器码。



## 相关链接

[Android 虚拟机发展史](https://juejin.im/post/5c232907f265da61662482b4)

[实现 ART 即时 \(JIT\) 编译器](https://source.android.com/devices/tech/dalvik/jit-compiler)

[安卓7.0为什么这么快](https://www.jianshu.com/p/9a3a4eb32a04)



