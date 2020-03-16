# APK 打包流程

![APK &#x6784;&#x5EFA;&#x6D41;&#x7A0B;&#x56FE;](../../.gitbook/assets/image%20%2834%29.png)

1. 编译器将您的源代码、三方库代码转换成 DEX 文件（Dalvik 可执行文件，其中包括在 Android 设备上运行的字节码），并将其他所有内容转换成编译后的资源。
2. APK 打包器将 DEX 文件和编译后的资源合并到一个 APK 中。不过，在将应用安装并部署到 Android 设备之前，必须先为 APK 签名。
3. APK 打包器使用调试或发布密钥库为 APK 签名：

* 如果您构建的是调试版应用（即专用于测试和分析的应用），则打包器会使用调试密钥库为应用签名。Android Studio 会自动使用调试密钥库配置新项目。
* 如果您构建的是打算对外发布的发布版应用，则打包器会使用发布密钥库为应用签名。要创建发布密钥库，请参阅在 Android Studio 中为应用签名。

    4. 在生成最终 APK 之前，打包器会使用 [zipalign ](https://developer.android.com/studio/command-line/zipalign.html)工具对应用进行优化，以减少其在设备上运  行时所占用的内存。 

* 如果您使用的是 [apksigner](https://developer.android.com/studio/command-line/apksigner.html)，则只能在为 APK 文件签名**之前**执行 zipalign。如果您在使用 apksigner 为 APK 签名之后对 APK 做出了进一步更改，签名便会失效。
* 如果您使用的是 [jarsigner](https://docs.oracle.com/javase/tutorial/deployment/jar/signing.html)，则只能在为 APK 文件签名**之后**执行 zipalign。

![Apk &#x8BE6;&#x7EC6;&#x6784;&#x5EFA;&#x6D41;&#x7A0B;](../../.gitbook/assets/image%20%2848%29.png)



[官方文档](https://developer.android.com/studio/build#build-process)

[10分钟了解Android项目构建流程](https://juejin.im/post/5a69c0ccf265da3e2a0dc9aa)

[内存对齐的规则以及作用](http://www.cppblog.com/snailcong/archive/2009/03/16/76705.html)

