# Service

## 服务类型

### 前台

前台服务用来执行用户能注意到的服务，比如音乐播放、文件下载（显示进度）。前台服务必须开启通知。

### 后台

运行不被用户注意的任务。API 26 对未在前台应用启动的后台任务有限制，推荐使用 JobScheduler。

### 绑定

组件可以绑定到服务中，然后利用其提供的C-S接口发送请求、获取结果甚至IPC调用。

服务既可以以启动方式运行，也可以以绑定方式运行，二者是可以同时进行的。

> **注意：**服务在其托管进程的主线程中运行，它既**不**创建自己的线程，也**不**在单独的进程中运行（除非另行指定）。如果服务将执行任何 CPU 密集型工作或阻止性操作（例如 MP3 播放或联网），则应通过在服务内创建新线程来完成这项工作。通过使用单独的线程，您可以降低发生“应用无响应”\(ANR\) 错误的风险，而应用的主线程仍可继续专注于运行用户与 Activity 之间的交互。

## 创建启动服务

如果组件通过调用 startService\(\) 启动服务（这会引起对 onStartCommand\(\) 的调用），则服务会一直运行，直到其使用 stopSelf\(\) 自行停止运行，或由其他组件通过调用 stopService\(\) 将其停止为止。

如果组件通过调用 bindService\(\) 来创建服务，且未调用 onStartCommand\(\)，则服务只会在该组件与其绑定时运行。当该服务与其所有组件取消绑定后，系统便会将其销毁。

> **注意**：为确保应用的安全性，在启动 [`Service`](https://developer.android.google.cn/reference/android/app/Service.html) 时，请始终使用显式 Intent，且不要为服务声明 Intent 过滤器。使用隐式 Intent 启动服务存在安全隐患，因为您无法确定哪些服务会响应 Intent，而用户也无法看到哪些服务已启动。从 Android 5.0（API 级别 21）开始，如果使用隐式 Intent 调用 [`bindService()`](https://developer.android.google.cn/reference/android/content/Context.html#bindService%28android.content.Intent,%20android.content.ServiceConnection,%20int%29)，则系统会抛出异常。

onStartCommand 返回值

* START\_NOT\_STICKY如果系统在 onStartCommand\(\) 返回后终止服务，则除非有待传递的挂起 Intent，否则系统不会重建服务。这是最安全的选项，可以避免在不必要时以及应用能够轻松重启所有未完成的作业时运行服务。
* START\_STICKY

如果系统在 onStartCommand\(\) 返回后终止服务，则其会重建服务并调用 onStartCommand\(\)，但不会重新传递最后一个 Intent。相反，除非有挂起 Intent 要启动服务，否则系统会调用包含空 Intent 的 onStartCommand\(\)。在此情况下，系统会传递这些 Intent。此常量适用于不执行命令、但无限期运行并等待作业的媒体播放器（或类似服务）。

* START\_REDELIVER\_INTENT

如果系统在 onStartCommand\(\) 返回后终止服务，则其会重建服务，并通过传递给服务的最后一个 Intent 调用 onStartCommand\(\)。所有挂起 Intent 均依次传递。此常量适用于主动执行应立即恢复的作业（例如下载文件）的服务。

停止服务

启动服务必须管理自己的生命周期。换言之，除非必须回收内存资源，否则系统不会停止或销毁服务，并且服务在 onStartCommand\(\) 返回后仍会继续运行。服务必须通过调用 stopSelf\(\) 自行停止运行，或由另一个组件通过调用 stopService\(\) 来停止它。

一旦请求使用 stopSelf\(\) 或 stopService\(\) 来停止服务，系统便会尽快销毁服务。

如果服务同时处理多个对 onStartCommand\(\) 的请求，则您不应在处理完一个启动请求之后停止服务，因为您可能已收到新的启动请求（在第一个请求结束时停止服务会终止第二个请求）。为避免此问题，您可以使用 stopSelf\(int\) 确保服务停止请求始终基于最近的启动请求。换言之，在调用 stopSelf\(int\) 时，您需传递与停止请求 ID 相对应的启动请求 ID（传递给 onStartCommand\(\) 的 startId）。此外，如果服务在您能够调用 stopSelf\(int\) 之前收到新启动请求，则 ID 不匹配，服务也不会停止。

## 创建绑定服务

## 生命周期

服务生命周期（从创建到销毁）可遵循以下任一路径：

### 启动服务

该服务在其他组件调用 startService\(\) 时创建，然后无限期运行，且必须通过调用 stopSelf\(\) 来自行停止运行。此外，其他组件也可通过调用 stopService\(\) 来停止此服务。服务停止后，系统会将其销毁。



![&#x670D;&#x52A1;&#x751F;&#x547D;&#x5468;&#x671F;](../../.gitbook/assets/image%20%281%29.png)

### 绑定服务

该服务在其他组件（客户端）调用 bindService\(\) 时创建。然后，客户端通过 IBinder 接口与服务进行通信。客户端可通过调用 unbindService\(\) 关闭连接。多个客户端可以绑定到相同服务，而且当所有绑定全部取消后，系统即会销毁该服务。（服务不必自行停止运行。）

这两条路径并非完全独立。您可以绑定到已使用 startService\(\) 启动的服务。例如，您可以使用 Intent（标识要播放的音乐）来调用 startService\(\)，从而启动后台音乐服务。随后，当用户需稍加控制播放器或获取有关当前所播放歌曲的信息时，Activity 可通过调用 bindService\(\) 绑定到服务。此类情况下，在所有客户端取消绑定之前，stopService\(\) 或 stopSelf\(\) 实际不会停止服务。

![&#x7ED1;&#x5B9A;&#x670D;&#x52A1;&#x751F;&#x547D;&#x5468;&#x671F;](../../.gitbook/assets/image%20%2818%29.png)

## 相关题目

Q：谈一谈Service的生命周期？

### 服务与子线程选择？

看是否需要在用户不与App交互时进行任务。需要则创建服务，不需要则创建子线程。

简单地说，服务是一种即使用户未与应用交互也可在后台运行的组件，因此，只有在需要服务时才应创建服务。如果您必须在主线程之外执行操作，但只在用户与您的应用交互时执行此操作，则应创建新线程。

* Q：Service的两种启动方式？区别在哪？
* Q：一个Activty先start一个Service后，再bind时会回调什么方法？此时如何做才能回调Service的destory\(\)方法？
* Q：Service如何和Activity进行通信？
* Q：用过哪些系统Service？
* * Q：是否能在Service进行耗时操作？如果非要可以怎么做？
* Q：AlarmManager能实现定时的原理？

### 前台服务是什么？和普通服务的不同？如何去开启一个前台服务？

前台服务是用户主动意识到的一种服务，因此在内存不足时，系统也不会考虑将其终止。前台服务必须为状态栏提供通知，将其放在_运行中的_标题下方。这意味着除非将服务停止或从前台移除，否则不能清除该通知。

如果应用面向 Android 9（API 级别 28）或更高版本并使用前台服务，则其必须请求 FOREGROUND\_SERVICE 权限。这是一种普通权限，因此，系统会自动为请求权限的应用授予此权限。

如果面向 API 级别 28 或更高版本的应用试图创建前台服务但未请求 FOREGROUND\_SERVICE，则系统会抛出 SecurityException。

* Q：是否了解ActivityManagerService，谈谈它发挥什么作用？
* Q：如何保证Service不被杀死？

