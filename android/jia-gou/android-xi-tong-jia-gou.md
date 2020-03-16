# Android 系统架构

Android 基于 Linux 开放源代码开发，系统架构图如下。

![Android &#x5E73;&#x53F0;&#x67B6;&#x6784;&#x56FE;](../../.gitbook/assets/image%20%2827%29.png)

## Linux 内核

Android 基础，Android 运行时依靠 Linux 执行底层功能，例如线程和内存管理。

## 硬件抽象层（HAL）

[硬件抽象层 \(HAL\)](https://source.android.google.cn/devices/architecture/hal-types) 提供标准接口，向更高级别的 Java API 框架显示**设备硬件功能**。HAL 包含多个库模块，其中每个模块都为特定类型的硬件组件实现一个接口，例如相机或蓝牙模块。当框架 API 要求访问设备硬件时，Android 系统将为该硬件组件加载库模块。

## Android 运行时

对于运行 Android 5.0（API 级别 21）或更高版本的设备，每个应用都在其自己的进程中运行，并且有其自己的 [Android Runtime \(ART\)](http://source.android.google.cn/devices/tech/dalvik/index.html) 实例。ART 执行DEX文件，DEX文件是专为Android平台设计的字节码格式，经过优化，使用的内存很小。

在5.0以前，Android Runtime是 Dalvik。

Android 还包含一套**核心运行时库**，可提供 Java API 框架所使用的 Java 编程语言中的大部分功能，包括一些 [Java 8 语言功能](https://developer.android.google.cn/guide/platform/j8-jack.html)。

## 原生C/C++库

许多核心 Android 系统组件和服务（例如 ART 和 HAL）使用 C 和 C++ 编写的原生库构建。Android 平台提供 Java 框架 API 以向应用显示其中部分原生库的功能。例如，您可以通过 Android 框架的 [Java OpenGL API](https://developer.android.google.cn/reference/android/opengl/package-summary.html) 访问 [OpenGL ES](https://developer.android.google.cn/guide/topics/graphics/opengl.html)，以支持在应用中绘制和操作 2D 和 3D 图形。

如果开发的是需要 C 或 C++ 代码的应用，可以使用 [Android NDK](https://developer.android.google.cn/ndk/index.html) 直接从原生代码访问某些[原生平台库](https://developer.android.google.cn/ndk/guides/stable_apis.html)。

## Java API 框架

Android 系统  Java API，可简化核心模块化系统组件和服务的重复使用，包括以下组件和服务：

* 丰富、可扩展的[视图系统](https://developer.android.google.cn/guide/topics/ui/overview.html)，可用以构建应用的 UI，包括列表、网格、文本框、按钮甚至可嵌入的网络浏览器
* [资源管理器](https://developer.android.google.cn/guide/topics/resources/overview.html)，用于访问非代码资源，例如本地化的字符串、图形和布局文件
* [通知管理器](https://developer.android.google.cn/guide/topics/ui/notifiers/notifications.html)，可让所有应用在状态栏中显示自定义提醒
* [Activity 管理器](https://developer.android.google.cn/guide/components/activities.html)，用于管理应用的生命周期，提供常见的[导航返回栈](https://developer.android.google.cn/guide/components/tasks-and-back-stack.html)
* [内容提供程序](https://developer.android.google.cn/guide/topics/providers/content-providers.html)，可让应用访问其他应用（例如“联系人”应用）中的数据或者共享其自己的数据

## 系统应用

系统预装的核心 App。比如相机、短信、电话、浏览器等。

