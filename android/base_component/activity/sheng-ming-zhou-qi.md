# 生命周期

### [官方文档](https://developer.android.com/guide/components/activities/?hl=zh-CN#Lifecycle)

![Activity &#x751F;&#x547D;&#x5468;&#x671F;](../../../.gitbook/assets/image%20%2819%29.png)

### onStart\(\)和onResume\(\)的区别？

onStart\(\) 调用后时 Activity 可见但是不可交互，onResume\(\) 调用后 Activity 处于前台并具有焦点，可以处理用户交互。

### onPause\(\)和onStop\(\)的区别？

onPause\(\) 时，另一个 Activity 位于屏幕前台并具有用户焦点，但此时的 Activity 仍可见。也就是说，另一个 Activity 显示在此 Activity 上方，并且**该 Activity 部分透明或未覆盖整个屏幕**。 暂停的 Activity 处于完全活动状态（[`Activity`](https://developer.android.com/reference/android/app/Activity.html?hl=zh-CN) 对象保留在内存中，它保留了所有状态和成员信息，并与窗口管理器保持连接），但在**内存极度不足的情况下，可能会被系统终止。不一定会调用onStop和onDestroy方法，所以应该尽量将重要数据保存的操作放到onPause方法中，但不能太耗时，否则会减免Activity 跳转速度，影响用户体验。**

onStop 时，Activity 被另一个Activity完全覆盖，处于「后台」状态，对用户不可见。 已停止的 Activity 同样仍处于活动状态（[`Activity`](https://developer.android.com/reference/android/app/Activity.html?hl=zh-CN) 对象保留在内存中，它保留了所有状态和成员信息，但_未_与窗口管理器连接）。 不过，**在需要释放内存时可能会被系统终止。**

### onCreate

首次创建Activity 时进行调用，需要调用setContentView设置布局。

### onDestroy

Activity 被销毁前调用。

### Activity A启动另一个Activity B会回调哪些方法？如果Activity B是完全透明呢？如果弹出的是一个Dialog呢？

Activity A 启动 Activity B：

1. Activity A 的 [`onPause()`](https://developer.android.com/reference/android/app/Activity.html?hl=zh-CN#onPause%28%29) 方法执行。
2. Activity B 的 [`onCreate()`](https://developer.android.com/reference/android/app/Activity.html?hl=zh-CN#onCreate%28android.os.Bundle%29)、[`onStart()`](https://developer.android.com/reference/android/app/Activity.html?hl=zh-CN#onStart%28%29) 和 [`onResume()`](https://developer.android.com/reference/android/app/Activity.html?hl=zh-CN#onResume%28%29) 方法依次执行。（Activity B 现在具有用户焦点。）
3. 然后，**如果 Activity A 在屏幕上不再可见**，则其 [`onStop()`](https://developer.android.com/reference/android/app/Activity.html?hl=zh-CN#onStop%28%29) 方法执行。

弹出 Dialog 不会影响生命周期回调，因为 Dialog 是通过在 Window 中添加 View 的方式展示的。

如果Activity B 是完全透明的，那么Activity A 此时是可见的，所以不会调用Activity A 的onStop 方法。

## 保存状态

### 谈谈onSaveInstanceState\(\)方法？何时会调用？

当系统为了恢复内存而销毁Activity前或者配置变更（屏幕方向、语言等）时，会调用这个方法，系统会传入一个Bundle 对象用于存储要恢复的数据。显式销毁（按下返回键）Activity时不会调用。

当Activity 重建时，可以在onCreate或者onRestoreInstanceState方法中拿到保存的数据。

![&#x4FDD;&#x5B58; Activity &#x72B6;&#x6001;](../../../.gitbook/assets/image%20%287%29.png)

Activity 的 onSaveInstanceState 的默认实现会调用 View 的onSaveInstanceState用于恢复View状态（需要为View 指定 id），比如EditText的输入文本等。

### onSaveInstanceState\(\) 与 onPause\(\) 的区别？

onPause\(\)是Activity生命周期的一部分，是明确会被调用的方法，而onSaveInstanceState方法并不能保证系统会调用，因此只应该利用它来记录 Activity 的瞬态（UI 的状态）；存储持久性数据应在 [`onPause()`](https://developer.android.com/reference/android/app/Activity.html?hl=zh-CN#onPause%28%29) 方法中进行。

