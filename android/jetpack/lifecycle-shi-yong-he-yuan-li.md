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

### 监听 Activity 生命周期

```text
//androidx.activity.ComponentActivity
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //...
    ReportFragment.injectIfNeededIn(this);
    //...
}
```

```text
public static void injectIfNeededIn(Activity activity) {
        if (Build.VERSION.SDK_INT >= 29) {
            // On API 29+, we can register for the correct Lifecycle callbacks directly
            activity.registerActivityLifecycleCallbacks(
                    new LifecycleCallbacks());
        }
        // Prior to API 29 and to maintain compatibility with older versions of
        // ProcessLifecycleOwner (which may not be updated when lifecycle-runtime is updated and
        // need to support activities that don't extend from FragmentActivity from support lib),
        // use a framework fragment to get the correct timing of Lifecycle events
        android.app.FragmentManager manager = activity.getFragmentManager();
        if (manager.findFragmentByTag(REPORT_FRAGMENT_TAG) == null) {
            manager.beginTransaction().add(new ReportFragment(), REPORT_FRAGMENT_TAG).commit();
            // Hopefully, we are the first to make a transaction.
            manager.executePendingTransactions();
        }
    }
```

ReportFragment

```text
// androidx.lifecycle.ReportFragment
@Override
public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    dispatchCreate(mProcessListener);
    dispatch(Lifecycle.Event.ON_CREATE);
}

@Override
public void onStart() {
    super.onStart();
    dispatchStart(mProcessListener);
    dispatch(Lifecycle.Event.ON_START);
}

@Override
public void onResume() {
    super.onResume();
    dispatchResume(mProcessListener);
    dispatch(Lifecycle.Event.ON_RESUME);
}

@Override
public void onPause() {
    super.onPause();
    dispatch(Lifecycle.Event.ON_PAUSE);
}
```

```text
private void dispatch(@NonNull Lifecycle.Event event) {
        if (Build.VERSION.SDK_INT < 29) {
            // Only dispatch events from ReportFragment on API levels prior
            // to API 29. On API 29+, this is handled by the ActivityLifecycleCallbacks
            // added in ReportFragment.injectIfNeededIn
            dispatch(getActivity(), event);
        }
    }
```

```text
// androidx.lifecycle.ReportFragment.LifecycleCallback
static class LifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(@NonNull Activity activity,
                @Nullable Bundle bundle) {
        }

        @Override
        public void onActivityPostCreated(@NonNull Activity activity,
                @Nullable Bundle savedInstanceState) {
            dispatch(activity, Lifecycle.Event.ON_CREATE);
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
        }

        @Override
        public void onActivityPostStarted(@NonNull Activity activity) {
            dispatch(activity, Lifecycle.Event.ON_START);
        }
}
```



```text
//androidx.lifecycle.ReportFragment
static void dispatch(@NonNull Activity activity, @NonNull Lifecycle.Event event) {
        if (activity instanceof LifecycleRegistryOwner) {
            ((LifecycleRegistryOwner) activity).getLifecycle().handleLifecycleEvent(event);
            return;
        }

        if (activity instanceof LifecycleOwner) {
            Lifecycle lifecycle = ((LifecycleOwner) activity).getLifecycle();
            if (lifecycle instanceof LifecycleRegistry) {
                ((LifecycleRegistry) lifecycle).handleLifecycleEvent(event);
            }
        }
    }
```

LifecycleRegistry

```text
/**
     * Sets the current state and notifies the observers.
     * <p>
     * Note that if the {@code currentState} is the same state as the last call to this method,
     * calling this method has no effect.
     *
     * @param event The event that was received
     */
    public void handleLifecycleEvent(@NonNull Lifecycle.Event event) {
        State next = getStateAfter(event);
        moveToState(next);
    }
```

## 监听器注解的处理时机

注解处理器生成 observe——adapter类。

```text
//androidx.lifecycle.LifycycleRegistry
@Override
public void addObserver(@NonNull LifecycleObserver observer) {
    State initialState = mState == DESTROYED ? DESTROYED : INITIALIZED;
    ObserverWithState statefulObserver = new ObserverWithState(observer, initialState);
    //....
    //ObserverWithState previous = mObserverMap.putIfAbsent(observer, statefulObserver);
}

static class ObserverWithState {
        State mState;
        LifecycleEventObserver mLifecycleObserver;

        ObserverWithState(LifecycleObserver observer, State initialState) {
            mLifecycleObserver = Lifecycling.lifecycleEventObserver(observer);
            mState = initialState;
        }

        void dispatchEvent(LifecycleOwner owner, Event event) {
            State newState = getStateAfter(event);
            mState = min(mState, newState);
            mLifecycleObserver.onStateChanged(owner, event);
            mState = newState;
        }
    }
```

```text
//androidx.lifecycle.LifeCycling
@NonNull
static LifecycleEventObserver lifecycleEventObserver(Object object) {
    // 根据不同的类型创建observer
    boolean isLifecycleEventObserver = object instanceof LifecycleEventObserver;
    boolean isFullLifecycleObserver = object instanceof FullLifecycleObserver;
    if (isLifecycleEventObserver && isFullLifecycleObserver) {
        return new FullLifecycleObserverAdapter((FullLifecycleObserver) object,
                (LifecycleEventObserver) object);
    }
    if (isFullLifecycleObserver) {
        return new FullLifecycleObserverAdapter((FullLifecycleObserver) object, null);
    }
    if (isLifecycleEventObserver) {
           return (LifecycleEventObserver) object;
    }

    final Class<?> klass = object.getClass();
    int type = getObserverConstructorType(klass);
    if (type == GENERATED_CALLBACK) {
        List<Constructor<? extends GeneratedAdapter>> constructors =
                sClassToAdapters.get(klass);
        if (constructors.size() == 1) {
            GeneratedAdapter generatedAdapter = createGeneratedAdapter(
                    constructors.get(0), object);
            return new SingleGeneratedAdapterObserver(generatedAdapter);
        }
        GeneratedAdapter[] adapters = new GeneratedAdapter[constructors.size()];
        for (int i = 0; i < constructors.size(); i++) {
            adapters[i] = createGeneratedAdapter(constructors.get(i), object);
        }
        return new CompositeGeneratedAdaptersObserver(adapters);
    }
    return new ReflectiveGenericLifecycleObserver(object);
}
```

## 监听应用生命周期

{% embed url="https://developer.android.com/reference/androidx/lifecycle/ProcessLifecycleOwner?hl=zh-cn" %}

## 自定义 LifeCyclerOwner

## 相关链接

* [官方链接](https://developer.android.com/topic/libraries/architecture/lifecycle?hl=zh-cn)
* [Android Jetpack架构组件（三）带你了解Lifecycle（原理篇）](http://liuwangshu.cn/application/jetpack/3-lifecycle-theory.html#post-comment)



