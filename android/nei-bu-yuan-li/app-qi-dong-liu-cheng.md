# App 启动流程

当点击`桌面App`的时候，发起进程就是`Launcher`所在的进程，启动远程进程，利用`Binder`发送消息给`system_server进程`；

  
在`system_server进程`中启动了N多服务，例如`ActiivityManagerService，WindowManagerService`等。启动进程的操作会先调用`AMS.startProcessLocked`方法，内部调用 `Process.start(android.app.ActivityThread);`而后通过`socket`通信告知`Zygote进程fork子进程`，即app进程。进程创建后将`ActivityThread`加载进去，执行`ActivityThread.main()`方法。

  
在`app进程`中，`main方法`会实例化`ActivityThread`，同时创建`ApplicationThread，Looper，Hander对象`，调用`attach方法`进行`Binder`通信，`looper`启动循环。`attach`方法内部获取`ActivityManagerProxy`对象，其实现了`IActivityManager`接口，作为客户端调用`attachApplication(mAppThread)`方法，将`thread`信息告知`AMS`。

在`system_server进程`中，`AMS`中会调用`ActivityManagerNative.onTransact`方法，真正的逻辑在服务端`AMS.attachApplication`方法中，内部调用`AMS.attachApplicationLocked`方法，方法的参数是`IApplicationThread`，在此处是`ApplicationThreadProxy`对象,用于跟前面通过`Process.start()`所创建的进程中`ApplicationThread`对象进行通信。  
`attachApplicationLocked`方法会处理`Provider, Activity, Service, Broadcast`相应流程，调用`ApplicationThreadProxy.bindApplication`方法，通过`Binder`通信，传递给`ApplicationThreadNative.onTransact`方法。

在`app进程`中，真正的逻辑在`ActivityThread.bindApplication`方法中。`bindApplication`方法的主要功能是依次向主线程发送消息`H.SET_CORE_SETTINGS 和H.BIND_APPLICATION`。后续创建`Application,Context`等。`Activity`的回调也会是通过Binder通信，然后发送不同消息处理。



[https://juejin.im/entry/586105ff61ff4b006308096a](https://juejin.im/entry/586105ff61ff4b006308096a)

{% embed url="https://www.jianshu.com/p/a5532ecc8377" %}

[http://gityuan.com/2016/10/09/app-process-create-2/](http://gityuan.com/2016/10/09/app-process-create-2/)

1. 2. Launcher startActivity
3. AMS startActivity
4. Zygote fork进程
5. Activity main\(\)
6. ActivityThread 进程loop循环
7. 开启Activity,开始生命周期回调…

