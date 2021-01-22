---
description: Jetpack  LifeCycle 库
---

# LifeCycle 使用和原理

## LifeCycle 是什么？

LifeCycle 是Jetpack 的一部分，它提供了一种机制让程序可以更加方便的响应响应 Android 组件（如Activity）的生命周期。

在此之前，如果一个组件需要响应 Android 组件的生命周期事件，通常可以在对应的生命周期回调里进行：

```text
internal class MyLocationListener(
        private val context: Context,
        private val callback: (Location) -> Unit
) {

    fun start() {
        // connect to system location service
    }

    fun stop() {
        // disconnect from system location service
    }
}

class MyActivity : AppCompatActivity() {
    private lateinit var myLocationListener: MyLocationListener

    override fun onCreate(...) {
        myLocationListener = MyLocationListener(this) { location ->
            // update UI
        }
    }

    public override fun onStart() {
        super.onStart()
        myLocationListener.start()
        // manage other components that need to respond
        // to the activity lifecycle
    }

    public override fun onStop() {
        super.onStop()
        myLocationListener.stop()
        // manage other components that need to respond
        // to the activity lifecycle
    }
}
```

但是上面的代码在真实的项目中会导致两个问题：

### 代码质量问题

* 可能有多个组件需要响应同一个 Activity 的生命周期，这就导致了Activity 的生命周期方法里充斥着处理各种组件的代码，不易维护
* 可能多个Activity 需要使用同一个组件，那么就需要在每个Activity的生命周期回调中处理这个组件，产生不必要的重复代码

### 内存泄漏问题

```text
//官网实例
class MyActivity : AppCompatActivity() {
    private lateinit var myLocationListener: MyLocationListener

    override fun onCreate(...) {
        myLocationListener = MyLocationListener(this) { location ->
            // update UI
        }
    }

    public override fun onStart() {
        super.onStart()
        Util.checkUserStatus { result ->
            // what if this callback is invoked AFTER activity is stopped?
            if (result) {
                myLocationListener.start()
            }
        }
    }

    public override fun onStop() {
        super.onStop()
        myLocationListener.stop()
    }

}
```

在上面例子中，如果Util.checkUserStatus 回调在onStop之后，就可能会导致MyActivity销毁后还在内存中，引发内存泄漏。 

通过 LifeCycle 提供的机制，可以将维护组件响应其他组件生命周期的操作集中在组件内部，提高代码的可维护性，简而言之就是解耦。

## 如何使用 LifeCycle 



```text
// 1. 实现 LifecycleObserver 接口
class MyObserver : LifecycleObserver {

    //2. 通过注解标记要响应的生命周期事件
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun connectListener() {
        ...
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun disconnectListener() {
        ...
    }
}

// 3. 添加监听
myLifecycleOwner.getLifecycle().addObserver(MyObserver())
```

组件之间通过事件来进行通信。

## 如何监听生命周期事件

## 如何处理生命周期事件



![Event &#x5F15;&#x53D1; State &#x8F6C;&#x6362;&#x793A;&#x610F;](../../.gitbook/assets/image%20%2858%29.png)

关系



## 监听器注解的处理时机

添加监听时通过反射进行处理。



## 监听应用生命周期

{% embed url="https://developer.android.com/reference/androidx/lifecycle/ProcessLifecycleOwner?hl=zh-cn" %}

## 自定义 LifeCyclerOwner

## 相关链接

* [官方链接](https://developer.android.com/topic/libraries/architecture/lifecycle?hl=zh-cn)
* [Android Jetpack架构组件（三）带你了解Lifecycle（原理篇）](http://liuwangshu.cn/application/jetpack/3-lifecycle-theory.html#post-comment)



