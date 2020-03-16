# 生命周期

### [官方文档](https://developer.android.com/guide/components/activities/?hl=zh-CN#Lifecycle)

![Activity &#x751F;&#x547D;&#x5468;&#x671F;](../../../.gitbook/assets/image%20%2837%29.png)

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

![&#x4FDD;&#x5B58; Activity &#x72B6;&#x6001;](../../../.gitbook/assets/image%20%2818%29.png)

Activity 的 onSaveInstanceState 的默认实现会调用 View 的onSaveInstanceState用于恢复View状态（需要为View 指定 id），比如EditText的输入文本等。

### onSaveInstanceState\(\) 与 onPause\(\) 的区别？

onPause\(\)是Activity生命周期的一部分，是明确会被调用的方法，而onSaveInstanceState方法并不能保证系统会调用，因此只应该利用它来记录 Activity 的瞬态（UI 的状态）；存储持久性数据应在 [`onPause()`](https://developer.android.com/reference/android/app/Activity.html?hl=zh-CN#onPause%28%29) 方法中进行。

## 其他问题

### 如何避免配置改变时 Activity 重建？（自行处理运行时配置变更）

在清单文件中编辑相应的 [`<activity>`](https://developer.android.com/guide/topics/manifest/activity-element.html?hl=zh-CN) 元素，增加 [`android:configChanges`](https://developer.android.com/guide/topics/manifest/activity-element.html?hl=zh-CN#config) 属性，该属性的值表示要处理的配置。最常用的值包括 `"orientation"`、`"screenSize"` 和 `"keyboardHidden"`。**`"orientation"` 值可在屏幕方向发生变更时阻止重启。`"screenSize"` 值也可在屏幕方向发生变更时阻止重启，但仅适用于 Android 3.2（API 级别 13）及以上版本的系统**。若想在应用中手动处理配置变更，必须在 `android:configChanges` 属性中声明 `"orientation"` 和 `"screenSize"` 值。可以在属性中声明多个配置值，方法是用`|` 字符将其进行分隔。

这样当其中某个配置发生变化，Activity 也不会重启。但会接收到对 [`onConfigurationChanged()`](https://developer.android.com/reference/android/app/Activity?hl=zh-CN#onconfigurationchanged) 的调用消息。此方法会收到传递的 [`Configuration`](https://developer.android.com/reference/android/content/res/Configuration.html?hl=zh-CN) 对象，从而指定新设备配置。可以通过读取 [`Configuration`](https://developer.android.com/reference/android/content/res/Configuration.html?hl=zh-CN) 中的字段确定新配置，然后通过更新界面所用资源进行适当的更改。调用此方法时，Activity 的 [`Resources`](https://developer.android.com/reference/android/content/res/Resources.html?hl=zh-CN) 对象会相应地进行更新，并根据新配置返回资源，以便在系统不重启 Activity 的情况下轻松重置界面元素。

