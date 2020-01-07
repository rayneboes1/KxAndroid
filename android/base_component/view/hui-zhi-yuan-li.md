# 绘制流程

## ActivityThread

```text
private void handleLaunchActivity(ActivityClientRecord r, Intent customIntent) {
    ...
    
    //这里是创建Activity,并调用了 Activity 的 onCreate()和onStart()
    Activity a = performLaunchActivity(r, customIntent);

    if (a != null) {
        r.createdConfig = new Configuration(mConfiguration);
        Bundle oldState = r.state;
        //这里调用Activity 的 onResume()
        handleResumeActivity(r.token, false, r.isForward,
                !r.activity.mFinished && !r.startsNotResumed);
    }
    ....
}
```

performLaunchActivity

```text
private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
    ......
    //这里底层是通过反射来创建的Activity实例
    java.lang.ClassLoader cl = r.packageInfo.getClassLoader();
    activity = mInstrumentation.newActivity(cl, component.getClassName(), r.intent);

    //底层也是通过反射构建Application,如果已经构建则不会重复构建,毕竟一个进程只能有一个Application
    Application app = r.packageInfo.makeApplication(false, mInstrumentation);

    if (activity != null) {
        Context appContext = createBaseContextForActivity(r, activity);
        CharSequence title = r.activityInfo.loadLabel(appContext.getPackageManager());
        Configuration config = new Configuration(mCompatConfiguration);
        if (DEBUG_CONFIGURATION) Slog.v(TAG, "Launching activity "
                + r.activityInfo.name + " with config " + config);
        //分析2 : 在这里实例化了PhoneWindow,并将该Activity设置为PhoneWindow的Callback回调,还初始化了WindowManager
        activity.attach(appContext, this, getInstrumentation(), r.token,
                r.ident, app, r.intent, r.activityInfo, title, r.parent,
                r.embeddedID, r.lastNonConfigurationInstances, config);

        //间接调用了Activity的performCreate方法,间接调用了Activity的onCreate方法.
        mInstrumentation.callActivityOnCreate(activity, r.state);
        
        //这里和上面onCreate过程差不多,调用Activity的onStart方法
        if (!r.activity.mFinished) {
            activity.performStart();
            r.stopped = false;
        }
        ....
    }
}
```

Instrumentation\#newActivity\(\)

```text
public Activity newActivity(ClassLoader cl, String className,
        Intent intent)
        throws InstantiationException, IllegalAccessException,
        ClassNotFoundException {
    //反射->实例化
    return (Activity)cl.loadClass(className).newInstance();
}
```

Activity\#attach\(\)

```text
final void attach(Context context, ActivityThread aThread,
        Instrumentation instr, IBinder token, int ident,
        Application application, Intent intent, ActivityInfo info,
        CharSequence title, Activity parent, String id,
        NonConfigurationInstances lastNonConfigurationInstances,
        Configuration config, String referrer, IVoiceInteractor voiceInteractor,
        Window window, ActivityConfigCallback activityConfigCallback) {
    ......

    //实例化PhoneWindow,关联PhoneWindow和Activity
    mWindow = new PhoneWindow(this, window, activityConfigCallback);
    mWindow.setWindowControllerCallback(this);
    mWindow.setCallback(this);
    mWindow.setOnWindowDismissedCallback(this);
    
    .....
    //还有一些其他的配置代码

    mWindow.setWindowManager(
            (WindowManager)context.getSystemService(Context.WINDOW_SERVICE),
            mToken, mComponent.flattenToString(),
            (info.flags & ActivityInfo.FLAG_HARDWARE_ACCELERATED) != 0);

    mWindowManager = mWindow.getWindowManager();
    ....
}

```

callOnCreate

```text
//首先是来到Instrumentation的callActivityOnCreate方法
public void callActivityOnCreate(Activity activity, Bundle icicle) {
    prePerformCreate(activity);
    activity.performCreate(icicle);
    postPerformCreate(activity);
}

//Activity#performCreate
final void performCreate(Bundle icicle) {
    performCreate(icicle, null);
}

final void performCreate(Bundle icicle, PersistableBundle persistentState) {
    ....
    //调用 Activity 的 onCreate
    if (persistentState != null) {
        onCreate(icicle, persistentState);
    } else {
        onCreate(icicle);
    }
    ......
}

```

perform start

```text
//Activity
final void performStart() {
    ......
    mInstrumentation.callActivityOnStart(this);
    ......
}

//Instrumentation
public void callActivityOnStart(Activity activity) {
    activity.onStart();
}

```

handleResume

```text
final void handleResumeActivity(IBinder token,
        boolean clearHide, boolean isForward, boolean reallyResume, int seq, String reason) {
    .....
    //分析1 : 在其内部调用Activity的onResume方法
    r = performResumeActivity(token, clearHide, reason);

    .....
    r.window = r.activity.getWindow();
    View decor = r.window.getDecorView();
    decor.setVisibility(View.INVISIBLE);
    //获取WindowManager
    ViewManager wm = a.getWindowManager();
    WindowManager.LayoutParams l = r.window.getAttributes();
    a.mDecor = decor;

    if (a.mVisibleFromClient) {
        .....
        //分析2 : WindowManager添加DecorView
        wm.addView(decor, l);
        ...
    }
    .....

}
```

```text
@Override
public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
    applyDefaultToken(params);
    mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
}
```



```text
public void addView(View view, ViewGroup.LayoutParams params,
        Display display, Window parentWindow) {
    ....

    ViewRootImpl root;

    synchronized (mLock) {
        root = new ViewRootImpl(view.getContext(), display);
        view.setLayoutParams(wparams);
        .....

        root.setView(view, wparams, panelParentView);
    }
}
```

调用 ViewRootImpl 的 setView 方法，讲decorView与ViewRootImpl关联。ViewRootImpl 中有一个 mView字段保存。

```text
public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
    .....
    requestLayout();
    .....
}

@Override
public void requestLayout() {
    if (!mHandlingLayoutInLayoutRequest) {
        //检查线程合法性
        checkThread();
        mLayoutRequested = true;
        scheduleTraversals();
    }
}
```

## ViewRootImpl

由 ViewRootImpl 的 scheduleTraversals 方法发起，方法通过给 Choreographer 发送一个callback，在下一帧时发起绘制流程。

```text
void scheduleTraversals() {
    if (!mTraversalScheduled) {
        mTraversalScheduled = true;
        mTraversalBarrier = mHandler.getLooper().getQueue().postSyncBarrier();
        //发送一个callback，下一帧时执行
        mChoreographer.postCallback(
                Choreographer.CALLBACK_TRAVERSAL, mTraversalRunnable, null);
        if (!mUnbufferedInputDispatch) {
            scheduleConsumeBatchedInput();
        }
        notifyRendererOfFramePending();
        pokeDrawLockIfNeeded();
    }
}
```

mTraversalRunnable 调用了 doTraversals:

```text
final class TraversalRunnable implements Runnable {
    @Override
    public void run() {
        doTraversal();
    }
}
```

doTraversal 调用了 performTraversals:

```text
void doTraversal() {
    if (mTraversalScheduled) {
        mTraversalScheduled = false;
        mHandler.getLooper().getQueue().removeSyncBarrier(mTraversalBarrier);

        performTraversals();
    }
}
```

真正绘制在 performTraversals 中进行，代码较长，[方法源码链接](https://cs.android.com/android/platform/superproject/+/master:frameworks/base/core/java/android/view/ViewRootImpl.java;bpv=0;bpt=0;l=1968) ，主要流程：

```text
private void performTraversals() {
    //开始测量流程，会调用 performMeasure 方法
    measureHierarchy(host, lp, res,
                    desiredWindowWidth, desiredWindowHeight);
    //开始布局流程
    performLayout(lp, mWidth, mHeight);
    //开始绘画流程
    performDraw();
}
```

performMeasure，调用顶层view的measure方法，开始测量

```text
private void performMeasure(int childWidthMeasureSpec, int childHeightMeasureSpec) {
    if (mView == null) {
        return;
    }
    try {
        mView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    } finally {
        Trace.traceEnd(Trace.TRACE_TAG_VIEW);
    }
}
```

performLayout，会调用view的layout方法，开始布局，performDarw 会调用draw方法，然后调用drawSoftWare，继而调用view的draw方法，分发绘制。

## View

### measure

measure 会调用 onMeasure，对于ViewGroup onMeasure 还对先测量每个子View，即调用它们的measure方法，向下传递measure过程

### layout

layout 会调用onLayout,ViewGroup 会通过调用子View的layou来向下传递布局过程。

### draw

```text
public void draw(Canvas canvas) {
    .....

    /*
        注意了这是官方给的注释,谷歌工程师还真是贴心,把draw步骤写的详详细细,给力,点赞
     * Draw traversal performs several drawing steps which must be executed
     * in the appropriate order:
     *
     *      1. Draw the background
     *      2. If necessary, save the canvas' layers to prepare for fading
     *      3. Draw view's content
     *      4. Draw children
     *      5. If necessary, draw the fading edges and restore layers
     *      6. Draw decorations (scrollbars for instance)
     */

    // Step 1, draw the background, if needed
    //1. 绘制背景
    if (!dirtyOpaque) {
        drawBackground(canvas);
    }

    // skip step 2 & 5 if possible (common case)
    final int viewFlags = mViewFlags;
    boolean horizontalEdges = (viewFlags & FADING_EDGE_HORIZONTAL) != 0;
    boolean verticalEdges = (viewFlags & FADING_EDGE_VERTICAL) != 0;
    if (!verticalEdges && !horizontalEdges) {
        // Step 3, draw the content
        //3. 绘制自己的内容
        if (!dirtyOpaque) onDraw(canvas);

        // Step 4, draw the children
        //4. 绘制子控件  如果是View的话这个方法是空实现,如果是ViewGroup则绘制子控件
        dispatchDraw(canvas);

        drawAutofilledHighlight(canvas);

        // Overlay is part of the content and draws beneath Foreground
        if (mOverlay != null && !mOverlay.isEmpty()) {
            mOverlay.getOverlayView().dispatchDraw(canvas);
        }

        // Step 6, draw decorations (foreground, scrollbars)
        //6. 绘制装饰和前景
        onDrawForeground(canvas);

        // Step 7, draw the default focus highlight
        //7. 绘制默认焦点高亮显示
        drawDefaultFocusHighlight(canvas);

        if (debugDraw()) {
            debugDrawFocus(canvas);
        }

        // we're done...
        return;
    }
    .....
}
```

dispatchDraw 会对每个子View 调用drawChild,继而调用子view的draw 方法，完成绘制分发。

[https://blog.csdn.net/xfhy\_/article/details/90270630](https://blog.csdn.net/xfhy_/article/details/90270630)

