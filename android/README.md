# Android

## Activity

* * Q：说下Activity的生命周期？
  * Q：onStart\(\)和onResume\(\)/onPause\(\)和onStop\(\)的区别？
  * Q：Activity A启动另一个Activity B会回调哪些方法？如果Activity B是完全透明呢？如果启动的是一个Dialog呢？
  * Q：谈谈onSaveInstanceState\(\)方法？何时会调用？
  * Q：onSaveInstanceState\(\)与onPause\(\)的区别？
  * Q：如何避免配置改变时Activity重建？
  * Q：优先级低的Activity在内存不足被回收后怎样做可以恢复到销毁前状态？
  * Q：说下Activity的四种启动模式？（有时会出个实际问题来分析返回栈中Activity的情况）
  * Q：谈谈singleTop和singleTask的区别以及应用场景
  * Q：onNewIntent\(\)调用时机？
  * Q：了解哪些Activity启动模式的标记位？
  * Q：如何启动其他应用的Activity？

### Activity的启动过程？

1. Activity startActivityForResult
2. Instrumentation execStartActivity
3. AMS startActivity
4. ApplicationThread scheduleLaunchActivity
5. ActivityThread.H handleMessage -&gt; performLaunchActivity
6. Activity attach
7. Instrumentation callActivityOnCreate

## Fragment

* * Q：谈一谈Fragment的生命周期？
  * Q：Activity和Fragment的异同？
  * Q：Activity和Fragment的关系？

### 何时会考虑使用Fragment？

* 当Activity需要模块化的时候
* 不同设备上的适配，比如平台和手机
* Activity相对Fragment而言，非常笨重，一般小界面小模块用Fragment比较合适，或者首页的tab之类的。

## Service

* * Q：谈一谈Service的生命周期？
  * Q：Service的两种启动方式？区别在哪？
  * Q：一个Activty先start一个Service后，再bind时会回调什么方法？此时如何做才能回调Service的destory\(\)方法？
  * Q：Service如何和Activity进行通信？
  * Q：用过哪些系统Service？
  * Q：是否能在Service进行耗时操作？如果非要可以怎么做？
  * Q：AlarmManager能实现定时的原理？
  * Q：前台服务是什么？和普通服务的不同？如何去开启一个前台服务？
  * Q：是否了解ActivityManagerService，谈谈它发挥什么作用？
  * Q：如何保证Service不被杀死？

## Broadcast Receiver

* * Q：广播有几种形式？什么特点？
  * Q：广播的两种注册形式？区别在哪？

## ContentProvider

* * Q：ContentProvider了解多少？

## 数据存储

### Android中提供哪些数据持久存储的方法？

* SharedPreferences: 小东西,最终是xml文件中,key-value的形式存储的.
* 文件
* 数据库
* ContentProvider
* 网络
* * Q：Java中的I/O流读写怎么做？
  * Q：SharePreferences适用情形？使用中需要注意什么？
  * Q：了解SQLite中的事务处理吗？是如何做的？
  * Q：使用SQLite做批量操作有什么好的方法吗？
  * Q：如果现在要删除SQLite中表的一个字段如何做？
  * Q：使用SQLite时会有哪些优化操作?

## IPC

* * Q：Android中进程和线程的关系？区别？
  * Q：为何需要进行IPC？多进程通信可能会出现什么问题？
  * Q：什么是序列化？Serializable接口和Parcelable接口的区别？为何推荐使用后者？
  * Q：Android中为何新增Binder来作为主要的IPC方式？
  * Q：使用Binder进行数据传输的具体过程？
  * Q：Binder框架中ServiceManager的作用？
  * Q：Android中有哪些基于Binder的IPC方式？简单对比下？
  * Q：是否了解AIDL？原理是什么？如何优化多模块都使用AIDL的情况？

## View

* * Q：MotionEvent是什么？包含几种事件？什么条件下会产生？
  * Q：scrollTo\(\)和scrollBy\(\)的区别？
  * Q：Scroller中最重要的两个方法是什么？主要目的是？

### LinearLayout 测量

先做一次测量，做完之后有空间剩余，有weight的View再测量一下，分一下剩余的空间。

### 屏幕适配

先前有鸿洋的AndroidAutoLayout，根据宽高进行控件缩放，非常经典，很多项目可能都还在使用，但是已经停止更新了。然后就是有名的今日头条方案，出来还是有点时间了。原理其实就是更改density。

屏幕的宽度=设计稿宽度 \* density。

然后有AndroidAutoSize库，将今日头条方案融合进去还完善了很多问题，易用，完美。

### View的事件分发机制？

事件传递大体过程：Activity--&gt; Window--&gt;DecorView --&gt; View树从上往下，传递过程中谁想拦截就拦截自己处理。MotionEvent是Android中的点击事件。主要事件类型：

* ACTION\_DOWN 手机初次触摸到屏幕事件
* ACTION\_MOVE 手机在屏幕上滑动时触发，会回调多次
* ACTION\_UP 手指离开屏幕时触发

需要关注的几个方法。

* dispatchTouchEvent\(event\);
* onInterceptTouchEvent\(event\);
* onTouchEvent\(event\);

上面3个方法可以用以下伪代码来表示其关系：

```text
public boolean dispatchTouchEvent(MotionEvent ev) {
    boolean consume = false;//事件是否被消费
    if (onInterceptTouchEvent(ev)) {//调用onInterceptTouchEvent判断是否拦截事件
        consume = onTouchEvent(ev);//如果拦截则调用自身的onTouchEvent方法
    } else {
        consume = child.dispatchTouchEvent(ev);//不拦截调用子View的dispatchTouchEvent方法
    }
    return consume;//返回值表示事件是否被消费，true事件终止，false调用父View的onTouchEvent方法
}
```

* * Q：如何解决View的滑动冲突？
  * Q：谈一谈View的工作原理？
  * Q：MeasureSpec是什么？有什么作用？
  * Q：自定义View/ViewGroup需要注意什么？

### onTouch\(\)、onTouchEvent\(\)和onClick\(\)关系？

如果一个View需要处理事件，它设置了OnTouchListener，那么OnTouchListener的onTouch方法会被回调。如果onTouch返回false,则onTouchEvent会被调用，反之不会。在onTouchEvent方法中，事件为Action.UP的时候会回调OnClickListener的onClick方法，可见OnClickListener的优先级很低。

* * Q：invalidate\(\)和postInvalidate\(\)的区别？

### 绘制原理

[https://blog.csdn.net/xfhy\_/article/details/90270630](https://blog.csdn.net/xfhy_/article/details/90270630)

### Android 换肤

重新设置LayoutInflater的Factory2，从而拦截创建View的过程，然后搞成自己的控件，想怎么换肤就怎么换肤。

### View 与 SurfaceView

* View是Android中所有控件的基类
* View适用于主动更新的情况，而SurfaceView则适用于被动更新的情况，比如频繁刷新界面。
* View在主线程中对页面进行刷新，而SurfaceView则开启一个子线程来对页面进行刷新。
* View在绘图时没有实现双缓冲机制，SurfaceView在底层机制中就实现了双缓冲机制。

## Drawable等资源

* * Q：了解哪些Drawable？适用场景？
  * Q：mipmap系列中xxxhdpi、xxhdpi、xhdpi、hdpi、mdpi和ldpi存在怎样的关系？
  * Q：dp、dpi、px的区别？
  * Q：res目录和assets目录的区别？

## Animation

* * Q：Android中有哪几种类型的动画？
  * Q：帧动画在使用时需要注意什么？
  * Q：View动画和属性动画的区别？
  * Q：View动画为何不能真正改变View的位置？而属性动画为何可以？
  * Q：属性动画插值器和估值器的作用？

## Window

* * Q：Activity、View、Window三者之间的关系？
  * Q：Window有哪几种类型？
  * Q：Activity创建和Dialog创建过程的异同？

## Handler

* * Q：谈谈消息机制Hander？作用？有哪些要素？流程是怎样的？
  * Q：为什么系统不建议在子线程访问UI？
  * Q：一个Thread可以有几个Looper？几个Handler？
  * Q：如何将一个Thread线程变成Looper线程？Looper线程有哪些特点？
  * Q：可以在子线程直接new一个Handler吗？那该怎么做？
  * Q：Message可以如何创建？哪种效果更好，为什么？
  * Q：这里的ThreadLocal有什么作用？
  * Q：主线程中Looper的轮询死循环为何没有阻塞主线程？
  * Q：使用Hanlder的postDealy\(\)后消息队列会发生什么变化？

## 线程

* * Q：Android中还了解哪些方便线程切换的类？
  * Q：AsyncTask相比Handler有什么优点？不足呢？
  * Q：使用AsyncTask需要注意什么？
  * Q：AsyncTask中使用的线程池大小？
  * Q：HandlerThread有什么特点？
  * Q：快速实现子线程使用Handler
  * Q：IntentService的特点？
  * Q：为何不用bindService方式创建IntentService？
  * Q：线程池的好处、原理、类型？
  * Q：ThreadPoolExecutor的工作策略？
  * Q：什么是ANR？什么情况会出现ANR？如何避免？在不看代码的情况下如何快速定位出现ANR问题所在？

## 图片加载

* * Q：加载图片的时候需要注意什么？
  * Q：LRU算法的原理？
  * Q：Android中缓存更新策略？
  * Picasso 原理
  * Glide 原理
  * 二者区别

## 性能优化

### App 启动流程？

1. Launcher startActivity
2. AMS startActivity
3. Zygote fork进程
4. Activity main\(\)
5. ActivityThread 进程loop循环
6. 开启Activity,开始生命周期回调…

需要调整

### 包体积优化？

可以使用lint工具，检测出没有用的文件。同时可以开启资源压缩（Proguard）,自动删除无用的资源。尽量多使用可绘制对象，某些图像不需要静态图像资源，框架可以在运行时动态绘制图像。尽量自己写Drawable，能不用UI切图就不用，占用空间小。

重用资源，比如一个三角按钮,点击前三角朝上代表收起的意思，点击后三角朝下，代表展开，一般情况下,我们会用两张图来切换，我们其实完全可以用旋转的形式去改变。比如同一图像的着色不同,我们可以用android:tint和tintMode属性，低版本可以使用ColorFilter。

压缩PNG和JPEG文件，可以减少PNG文件的大小，而不会丢失图像质量。使用WebP文件格式，可以使用WebP文件格式，而不是使用PNG或JPEG文件。可以使用AS将现有的BMP、JPG、PNG或静态GIF图像转换成WebP格式。使用矢量图形.svg；代码混淆，使用proGuard代码混淆器工具,它包括压缩，优化，混淆等功能。这个大家太熟悉。插件化，将功能模块放服务器上，按需下载，可以减少安装包大小。

### 启动优化？

利用提前展示出来的Window，快速展示出来一个节目，给用户快速反馈的体验。障眼法，治标不治本。

避免在启动时做密集沉重的初始化\(Heavy app initialization\)。某些SDK初始化放在异步去加载\(比如友盟，bugly这样的业务非必要可以异步加载\)，比如地图，推送等，非第一时间需要的可以在主线程做延时启动\(比如闪屏页\)，当程序已经启动起来之后,再进行初始化。对于网络，图片请求框架就必须在主线程中初始化了。

启动时，避免I/O操作，反序列化，网络操作，布局嵌套等耗时操作。

### 布局优化？

* 如果父控件有颜色，也是自己需要的颜色，那么就不必在子控件加背景颜色
* 如果子控件有背景颜色，并且能完全覆盖父控件，那么父控件不用设置背景颜色
* 尽量减少不必要的嵌套
* 能用LinearLayout和FrameLayout，就不要用RelativeLayout，因为RelativeLayout相对比较复杂，测绘也相对耗时。
* include和merge一起使用，增加复用，减少层级
* ViewStub按需加载，更加轻便
* 复杂界面选择ConstraintLayout，可有效减少层级

### 内存优化？

* 频繁使用字符串拼接用StringBuilder或者StringBuffer；ArrayMap、SparseArray替换HashMap；避免内存泄漏。
* 集合类泄漏\(集合一直引用着被添加进来的元素对象\)
* 单例/静态变量造成的内存泄漏\(生命周期长的持有了生命周期短的引用\)
* 匿名内部类/非静态内部类
* 资源未关闭造成的内存泄漏
* 检测内存泄漏的几个工具: LeakCanary，TraceView，Systrace，Android Lint和Memory Monitor+mat

### 内存泄漏

* 集合类泄漏\(集合一直引用着被添加进来的元素对象\)
* 单例/静态变量造成的内存泄漏\(生命周期长的持有了生命周期短的引用\)
* 匿名内部类/非静态内部类
* 资源未关闭造成的内存泄漏
* 网络，文件等流忘记关闭
* 手动注册广播时，退出时忘记unregisterReceiver\(\)
* Service执行完成后忘记stopSelf\(\)
* EventBus等观察者模式的框架忘记手动解除注册

### App 线程优化？

线程池避免存在大量的Thread，重用线程池内部的线程，从而避免了线程的创建和销毁带来的性能开销，同时能有效控制线程池的最大并发数，避免大量线程因互相抢占系统资源而导致阻塞线现象发生。推荐阅读《Android开发艺术探索》 第11章。

**分类**

* FixedThreadPool 数量固定的线程池
* CachedThreadPool 只有非核心线程，数量不定，空闲线程有超时机制，比较适合执行大量耗时较少的任务
* ScheduledThreadPool 核心线程数量固定，非核心线程没有限制。主要用于执行定时任务和具有固定中周期的重复任务。
* SingleThreadPool 只有一个核心线程，确保所有的任务在同一个线程顺序执行，统一外界任务到一个线程中，这使得在这些任务之间不需要处理线程同步的问题。

**优点**

* 减少在创建和销毁线程上所花的时间以及系统资源的开销
* 不使用线程池有可能造成系统创建大量的线程而导致消耗完系统内存以及"过度切换"

**注意点**

* 如果线程池中的数量未达到核心线程的数量,则直接启动一个核心线程来执行任务
* 如果线程池中的数量已经达到或超过核心线程的数量,则任何会被插入到任务队列中等待执行
* 如果2中的任务无法插入到任务队列中,由于任务队列已满,这时候如果线程数量未达到线程池规定的最大值,则会启动一个非核心线程来执行任务
* 如果3中的线程数量已经达到线程池最大值,则会拒绝执行此任务,ThreadPoolExecutor会调用RejectedExecutionHandler的rejectedExecution\(\)方法通知调用者
* * Q：项目中如何做性能优化的？
  * Q：了解哪些性能优化的工具？
  * Q：布局上如何优化？列表呢？
  * Q：内存泄漏是什么？为什么会发生？常见哪些内存泄漏的例子？都是怎么解决的？
  * Q：内存泄漏和内存溢出的区别？
  * Q：什么情况会导致内存溢出？

## 架构

### Android 架构图

![](https://uploader.shimo.im/f/IBgZvkOMNOUmnfdf.png!thumbnail)

### MVP/MVC/MVVM

**MVC**

在Android中View和Controller一般就是被Activity充当了，当逻辑非常多，操作非常复杂时，Activity代码量非常庞大，不易维护。

* Model : 模型层，业务逻辑+数据存储等
* View : 用户界面，一般就是xml+Activity
* Controller : 控制层，一般就是Activity

**MVP**

我个人角度，现在\(2019年10月29日20:02:49\)大多是使用这种方式，既不复杂也解耦合了。

* Model：模型层，业务逻辑+数据存储+网络请求
* View：视图层，View绘制和用户交互等，一般是Activity
* Presenter：呈现层，连接V层和M层，完成他们之间的交互

接口过多，对增加和修改很不友好

**MVVM**

为了更加分离M，V层，所以有了MVVM。

* Model：模型层，业务逻辑+数据存储+网络请求
* View：视图层，View绘制和用户交互等，一般是Activity
* ViewModel：其实就是Presenter和View的数据模型的合体。双向绑定，View的变动会反应到ViewModel中，数据的变动也会反应到View上。

### 组件化的好处

1. 任意修改都需要编译整个工程，效率低下，组件化可以单独编译工程。
2. 解耦，有利于多人团队协作开发
3. 功能复用

## 谷歌新动态

### 是否了解和使用过谷歌推出的新技术？

compose jetpack

### 有了解刚发布的Androidx.0的特性吗？

**Android 5.0**

* Material Design
* ART虚拟机

**Android 6.0**

* 应用权限管理
* 官方指纹支持
* Doze电量管理
* 运行时权限机制-&gt;需要动态申请权限

**Android 7.0**

* 多窗口模式
* 支持Java 8语言平台
* 需要使用FileProvider访问照片
* 安装apk需要兼容

**Android 8.0**

* 通知
* 画中画
* 自动填充
* 后台限制
* 自适应桌面图标-&gt;适配
* 隐式广播限制
* 开启后台Service限制
* clear text not permitted

**Android 9.0**

* 利用 Wi-Fi RTT 进行室内定位
* 刘海屏 API 支持
* 多摄像头支持和摄像头更新
* 不允许调用hide api
* 限制明文流量的网络请求 http

**Android 10**

* 暗黑模式
* 隐私增强\(后台能否访问定位\)
* 限制程序访问剪贴板
* 应用黑盒
* 权限细分需兼容
* 后台定位单独权限需兼容
* 设备唯一标示符需兼容
* 后台打开Activity 需兼容
* 非 SDK 接口限制 需兼容

### Kotlin对Java做了哪些优化？

* 完全兼容java
* 空安全
* 支持lambda表达式
* 支持扩展函数
* 更少的代码量,更快的开发速度

缺点就是有时候代码阅读性可能会降低，生成class 可能比Java大，集成后包体积也会增大。

## 其他

### 热修复原理

1. 安卓在加载class时会通过**双亲委托机制**去加载一个类，先让父类去加载，如果找不到再让子类去加载某个类。
2. 通过查看ClassLoader源码发现findClass方法是由每个子类自己实现的，比如BootClassLoader或者BaseDexClassLoader。而PathClassLoader是继承自BaseDexClassLoader的，它的findClass也是在BaseDexClassLoader里面实现的。 
3. BaseDexClassLoader的findClass里面使用了另一个对象DexPathList去查找对应的class，这是安卓里面特有的实现。在DexPathList对象里面有一个属性dexElements，dexElements是用于存放加载好了的dex数组的，查找class是从这个dexElements数组里面去找的。 
4. dexElements里面存放的是Element对象，findClass最终会交给Element去实现，Element又会交给Element里面的一个属性DexFile去实现。我看了下，最终是用native实现的。 
5. 回到上面的第3步中的DexPathList对象从dexElements数组里面查找class，从数组的前面往后找,找到了就返回结果，不再继续查找。
6. 所以当我们把修复好bug了的class，搞成dex,然后通过反射等技术放到dexElements的最前面，这样系统在通过PathClassLoader找到class时，就能先找到我们放置的修复好bug的class，然后就不会再往后找了，相当于实现了热修复。这样有bug的class就不会被用了。应了一句古话，近水楼台先得月。
7. 第6点中的反射，流程是：获取到PathClassLoader，然后反射获取到父类中的DexPathList对象，然后再反射到DexPathList对象中的dexElements数组。然后将补丁\(dex\)转为Element对象，插入到dexElements数组的前面\(先复制出来，再合并，再通过反射放回去\)。

一句话总结。将修复好的类放在dexElements的最前面，这样在加载类的时候就会被优先加载到而达到修复的目的。

