---
description: Jetpack  LifeCycle 库
---

# LifeCycle

## LifeCycle 解决了什么问题？

LifeCycle 是 Android Jetpack 的一部分，它提供了一种机制可以让程序可以更加方便的响应 Android 组件（如 Activity）的生命周期。

在此之前，如果一个组件需要响应 Android 组件的生命周期事件，通常可以在对应的生命周期回调里进行，例如下面的代码示例：

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

但是上面的代码会导致两个问题：

### 代码质量问题

* 可能有多个组件需要响应同一个 Activity 的生命周期，这就导致了Activity 的生命周期方法里充斥着处理各个组件的代码，不易维护
* 可能多个Activity 需要使用同一个组件，那么就需要在每个Activity的生命周期回调中都添加处理这个组件的逻辑，产生了不必要的重复代码

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

在上面例子中，如果Util.checkUserStatus 回调在onStop之后，就可能会导致MyActivity销毁后还在内存中，引发 Activity 泄漏。 

通过 LifeCycle 提供的机制，可以将组件响应生命周期的操作集中在组件内部，提高代码的可维护性，简而言之就是解耦。

## 如何使用 LifeCycle 

### 集成lifecycle库

```text
dependencies {
    def lifecycle_version = "2.2.0"

    // 可以通过 ViewModel/LiveData 间接引用
    https://developer.android.com/jetpack/androidx/releases/lifecycle#declaring_dependencies
    
    // Lifecycles only (without ViewModel or LiveData)
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version"

    // Annotation processor
    kapt "androidx.lifecycle:lifecycle-compiler:$lifecycle_version"
    // alternately - if using Java8, use the following instead of lifecycle-compiler
    implementation "androidx.lifecycle:lifecycle-common-java8:$lifecycle_version"
}
```

可以通过ViewModel或者LiveData来间接集成Lifecycle库，这里以只依赖Lifecycle库来举例，是否集成注解处理库，会影响Observer创建方式，不集成时通过反射调用。

### 创建监听器

使用LifecycleObserver+注解

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

使用 LifecycleEventObserver

```text
class MyLifecycleEventObserver : LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        //do something
    }

    //不会被调用
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun resolveCreate() {
        //never called

    }
}
```

上面被OnLifeCycleEvent注解标记的方法不会被自动调用，因为在LifecycleEventObserver注释中可以看到：

> If a class implements this interface and in the same time uses OnLifecycleEvent, then annotations will be ignored.

组件之间通过事件来进行通信。



带有参数的情况。 方便处理监听多个LifecycleOwener的情况，或者对Event统一处理。

```text
 class TestObserver implements LifecycleObserver {
   @OnLifecycleEvent(ON_CREATE)
   void onCreated(LifecycleOwner source) {}
   @OnLifecycleEvent(ON_ANY)
   void onAny(LifecycleOwner source, Event event) {}
 }
```

### 添加监听器



## 监听生命周期的源码

## 处理生命周期事件源码



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
//参数是 object
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

如果集成了注解处理库，就会生成对应的Adapter类，就不会进行反射调用；否则会通过反射进行调用。

## 监听应用生命周期

集成依赖

```text
implementation "androidx.lifecycle:lifecycle-process:$lifecycle_version"
```

{% embed url="https://developer.android.com/reference/androidx/lifecycle/ProcessLifecycleOwner?hl=zh-cn" %}

## 自定义 LifeCyclerOwner





## 一些小问题

使用Java 8 开发时可以通过集成 common-java8 并继承 DefaultLifecycleObserver，重写对应的生命周期方法即可，从而可以避免注解处理过程。DefaultLifecycleObserver 是一个接口，为每个回调方法提供了空的实现。



## 相关链接

* [官方链接](https://developer.android.com/topic/libraries/architecture/lifecycle?hl=zh-cn)
* [Android Jetpack架构组件（三）带你了解Lifecycle（原理篇）](http://liuwangshu.cn/application/jetpack/3-lifecycle-theory.html#post-comment)



