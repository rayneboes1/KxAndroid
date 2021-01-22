---
description: Jetpack  LifeCycle 库
---

# LifeCycle 使用和原理

## LifeCycle 是什么？

LifeCycle 是一个可以用来响应 Android 中组件生命周期的框架。

如果一个组件需要响应Android组件的生命周期事件，通常可以在对应的回调里添加。但是这会导致两个问题：

### 代码质量问题

* 可能有多个组件需要响应同一个Activity的生命周期，这就导致了Activity 的生命周期里充斥着处理各种组件的代码，不易维护
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

通过LifeCycle提供的机制，可以将维护组件响应其他组件生命周期的操作集中在组件内部，简而言之就是解耦。

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

## 如何监听生命周期事件

## 如何处理生命周期事件



![Event &#x5F15;&#x53D1; State &#x8F6C;&#x6362;&#x793A;&#x610F;](../../.gitbook/assets/image%20%2858%29.png)

关系



## 监听器注解的处理时机

添加监听时通过反射进行处理。

## 相关链接

* [http://liuwangshu.cn/application/jetpack/3-lifecycle-theory.html\#post-comment](http://liuwangshu.cn/application/jetpack/3-lifecycle-theory.html#post-comment)



