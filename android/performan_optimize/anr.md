# ANR

## 发生条件

主线程由于耗时操作而不能正确响应用户输入事件。

> 确保高效的计算始终至关重要，但即使最高效的代码仍然需要时间来运行。

在 Android 中，应用响应性由 **Activity 管理器和窗口管理器**系统服务监控。当 Android 检测到以下某一项条件时，便会针对特定应用显示 ANR 对话框：

* 在 5 秒内对输入事件（例如按键或屏幕轻触事件）没有响应。
* BroadcastReceiver 在 10 秒后尚未执行完毕。

## 避免 ANR

在主线程中运行的所有方法都应该尽可能减少在此线程中的操作。**特别是在 onCreate\(\) 和 onResume\(\) 等关键生命周期方法中，Activity 应尽量减少进行设置所需的操作。**

可能会长时间运行的操作（例如网络或数据库操作）或计算成本高昂的计算（例如调整位图大小）应在**工作线程**中完成（如果是数据库操作，则应通过异步请求完成）。

创建自己的 `Thread` 或 `HandlerThread` 类时应该调用 Process.setThreadPriority\(\) 并传递 `THREAD_PRIORITY_BACKGROUND`从而**将线程优先级设为“后台”优先**。如果不通过这种方式将线程设为较低的优先级，则此线程仍可能会让应用变慢，因为默认情况下，此线程会按照与界面线程相同的优先级操作。

对 BroadcastReceiver 执行时间的特定约束强调了广播接收器的功能：在后台执行少量离散工作，例如保存设置或注册 Notification。因此，与在界面线程中调用的其他方法一样，应用应避免在广播接收器中执行可能会长时间运行的操作或计算。但如果需要执行可能需要长时间运行的操作以响应 intent 广播，则应用应启动 `IntentService`，而不是通过工作线程执行密集型操作。

## ANR 检测

### StrictMode 

可以支持设置线程策略和虚拟机策略，在开发过程中使用。

#### 线程策略 ThreadPolicy

* detectCustomSlowCalls：检测自定义耗时操作
* detectDiskReads：检测是否存在磁盘读取操作
* detectDiskWrites：检测是否存在磁盘写入操作
* detectNetWork：检测是否存在网络操作

#### 虚拟机策略VmPolicy

* detectActivityLeaks：检测是否存在Activity泄露
* detectLeakedClosableObjects：检测是否存在未关闭的Closeable对象泄露
* detectLeakedSqlLiteObjects：检测是否存在Sqlite对象泄露
* setClassInstanceLimit：检测类实例个数是否超过限制

### 其他工具

#### BlockCanary

在 Looper 的 loop 方法里有如下代码：

```text
.....
final Printer logging = me.mLogging;
if (logging != null) {
    logging.println(">>>>> Dispatching to " + msg.target + " " +
                msg.callback + ": " + msg.what);
}
....
msg.target.dispatchMessage(msg);
...
if (logging != null) {
    logging.println("<<<<< Finished to " + msg.target + " " + msg.callback);
}
```

而 Looper 提供了 setMessageLogging 方法设置logging，通过给Looper设置自定义logging，记录消息处理前后时间戳，计算消息处理耗时，如果过长则说明出现了ANR。

#### ANR-watchDog

在一个子线程中不断向主线程发送消息，消息的callback 就是更新某个变量的值，停止一段时间后对该值进行检查，如果变量的值还是上一次的值，则说明主线程已经出现卡顿或ANR了。

#### TAKT（检测FPS）

Android系统从4.1\(API 16\)开始加入 Choreographer 这个类来控制同步处理输入\(Input\)、动画\(Animation\)、绘制\(Draw\)三个UI操作。UI显示每一帧时要完成的事情只有这三种。

![&#x4E22;&#x5E27;](../../.gitbook/assets/image%20%2812%29.png)

Choreographer 接收显示系统的时间脉冲\(垂直同步信号-VSync信号\)，在下一个frame渲染时控制执行这些操作。  
通过`Choreographer.getInstance().postFrameCallback(new FPSFrameCallback())`把自定义回调添加到Choreographer之中，那么在下一个frame被渲染的时候就会回调callback.

通过判断两次回调 doFrame 执行的时间差，来判断是否发生ANR。

## 参考博客

[使应用能迅速响应](https://developer.android.com/training/articles/perf-anr?hl=zh-CN)

[Android UI 卡顿及ANR检测原理](https://www.jianshu.com/p/a7dfac037c4c)

{% embed url="https://duanqz.github.io/2015-10-12-ANR-Analysis\#21-anr%E7%9A%84%E7%9B%91%E6%B5%8B%E6%9C%BA%E5%88%B6" %}



