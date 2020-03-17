# 启动流程

Activity startActivity

```text
@Override
public void startActivity(Intent intent) {
	startActivity(intent, null);
}
 
@Override
public void startActivity(Intent intent, Bundle options) {
	if (options != null) {
		startActivityForResult(intent, -1, options);
	} else {
		// Note we want to go through this call for compatibility with
		// applications that may have overridden the method.
		startActivityForResult(intent, -1);
	}
}
 
public void startActivityForResult(Intent intent, int requestCode) {
	startActivityForResult(intent, requestCode, null);
}
```

startActivityForResult

```text
public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
	  //一般的Activity其mParent为null，mParent常用在ActivityGroup中，ActivityGroup已废弃
	  if (mParent == null) {
		    //这里会启动新的Activity，核心功能都在mMainThread.getApplicationThread()中完成
		    Instrumentation.ActivityResult ar =
			      mInstrumentation.execStartActivity(
				        this, mMainThread.getApplicationThread(), mToken, this,
				        intent, requestCode, options);
		if (ar != null) {
		    //发送结果，即onActivityResult会被调用
			  mMainThread.sendActivityResult(
				    mToken, mEmbeddedID, requestCode, ar.getResultCode(),
				     ar.getResultData());
		}
		if (requestCode >= 0) {
			// If this start is requesting a result, we can avoid making
			// the activity visible until the result is received.  Setting
			// this code during onCreate(Bundle savedInstanceState) or onResume() will keep the
			// activity hidden during this time, to avoid flickering.
			// This can only be done when a result is requested because
			// that guarantees we will get information back when the
			// activity is finished, no matter what happens to it.
			    mStartedActivity = true;
		}
 
		final View decor = mWindow != null ? mWindow.peekDecorView() : null;
		if (decor != null) {
			decor.cancelPendingInputEvents();
		}
	} else {
		//在ActivityGroup内部的Activity调用startActivity的时候会走到这里，内部处理逻辑和上面是类似的
		if (options != null) {
			mParent.startActivityFromChild(this, intent, requestCode, options);
		} else {
			// Note we want to go through this method for compatibility with
			// existing applications that may have overridden it.
			mParent.startActivityFromChild(this, intent, requestCode);
		}
	}
}
```

Instrumentation execStartActivity

```text
public ActivityResult execStartActivity(
		  Context who, IBinder contextThread, IBinder token, Activity target,
		Intent intent, int requestCode, Bundle options) {
	  //核心功能在这个whoThread中完成，其内部scheduleLaunchActivity方法用于完成activity的打开
	  IApplicationThread whoThread = (IApplicationThread) contextThread;
	  if (mActivityMonitors != null) {
		    synchronized (mSync) {
			  //先查找一遍看是否存在这个activity
			  final int N = mActivityMonitors.size();
			  for (int i=0; i<N; i++) {
				    final ActivityMonitor am = mActivityMonitors.get(i);
				    if (am.match(who, null, intent)) {
					  //如果找到了就跳出循环
					  am.mHits++;
					   //如果目标activity无法打开，直接return
					   if (am.isBlocking()) {
						    return requestCode >= 0 ? am.getResult() : null;
					   }
					   break;
				}
			}
		}
	}
	try {
		 intent.migrateExtraStreamToClipData();
		 intent.prepareToLeaveProcess();
		 //这里才是真正打开activity的地方，核心功能在whoThread中完成。
		 int result = ActivityManagerNative.getDefault()
			    .startActivity(whoThread, who.getBasePackageName(), intent,
		 intent.resolveTypeIfNeeded(who.getContentResolver()),
				token, target != null ? target.mEmbeddedID : null,
				requestCode, 0, null, null, options);
		 //这个方法是专门抛异常的，它会对结果进行检查，如果无法打开activity，
		 //则抛出诸如ActivityNotFoundException类似的各种异常
		 checkStartActivityResult(result, intent);
	 } catch (RemoteException e) {
	 }
	 return null;
}
```

ActivityManagerNative\#getDefault

```text
static public IActivityManager getDefault() {
    return ActivityManager.getService();
}
```

ActivityManager\#getService

```text
public static IActivityManager getService() {
        return IActivityManagerSingleton.get();
}

private static final Singleton<IActivityManager> IActivityManagerSingleton =
            new Singleton<IActivityManager>() {
                @Override
                protected IActivityManager create() {
                    //获取系统服务
                    final IBinder b = ServiceManager.getService(Context.ACTIVITY_SERVICE);
                    final IActivityManager am = IActivityManager.Stub.asInterface(b);
                    return am;
                }
    };
```

IActivityManger 的实现类是 ActivityManagerService， startActivity 方法

```text
@Override
public int startActivity(IApplicationThread caller, String callingPackage,
            Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
            int startFlags, ProfilerInfo profilerInfo, Bundle bOptions) {
    return mActivityTaskManager.startActivity(caller, callingPackage, intent, resolvedType,
        resultTo, resultWho, requestCode, startFlags, profilerInfo, bOptions);
}
```

ActivityTaskManagerService\#startactivity

```text
@Override
public final int startActivity(IApplicationThread caller, String callingPackage,
    Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode,
            int startFlags, ProfilerInfo profilerInfo, Bundle bOptions) {
    return startActivityAsUser(caller, callingPackage, intent, resolvedType, resultTo,
                resultWho, requestCode, startFlags, profilerInfo, bOptions,
                UserHandle.getCallingUserId());
}
```

ActivityStarter\#excute

```text
int execute() {
        try {
            // TODO(b/64750076): Look into passing request directly to these methods to allow
            // for transactional diffs and preprocessing.
            if (mRequest.mayWait) {
                return startActivityMayWait(mRequest.caller, mRequest.callingUid,
                        mRequest.callingPackage, mRequest.realCallingPid, mRequest.realCallingUid,
                        mRequest.intent, mRequest.resolvedType,
                        mRequest.voiceSession, mRequest.voiceInteractor, mRequest.resultTo,
                        mRequest.resultWho, mRequest.requestCode, mRequest.startFlags,
                        mRequest.profilerInfo, mRequest.waitResult, mRequest.globalConfig,
                        mRequest.activityOptions, mRequest.ignoreTargetSecurity, mRequest.userId,
                        mRequest.inTask, mRequest.reason,
                        mRequest.allowPendingRemoteAnimationRegistryLookup,
                        mRequest.originatingPendingIntent, mRequest.allowBackgroundActivityStart);
            } else {
                return startActivity(mRequest.caller, mRequest.intent, mRequest.ephemeralIntent,
                        mRequest.resolvedType, mRequest.activityInfo, mRequest.resolveInfo,
                        mRequest.voiceSession, mRequest.voiceInteractor, mRequest.resultTo,
                        mRequest.resultWho, mRequest.requestCode, mRequest.callingPid,
                        mRequest.callingUid, mRequest.callingPackage, mRequest.realCallingPid,
                        mRequest.realCallingUid, mRequest.startFlags, mRequest.activityOptions,
                        mRequest.ignoreTargetSecurity, mRequest.componentSpecified,
                        mRequest.outActivity, mRequest.inTask, mRequest.reason,
                        mRequest.allowPendingRemoteAnimationRegistryLookup,
                        mRequest.originatingPendingIntent, mRequest.allowBackgroundActivityStart);
            }
        } finally {
            onExecutionComplete();
        }
    }
```

最终会调到 ApplicationThread scheduleLaunchActivity

```text
public final void scheduleLaunchActivity() {
    // 通过 Handler 发送 LAUNCH_ACTIVITY
    sendMessage(H.LAUNCH_ACTIVITY, r);
}
```

ActivityThread\#handleLaunchActivity

```text
private void handleLaunchActivity() {
    // 执行启动 Activity
    Activity a = performLaunchActivity(r, customIntent);
    if (a != null) {
        // resume activity
        handleResumeActivity(r.token, false, r.isForward,
                    !r.activity.mFinished && !r.startsNotResumed, r.lastProcessedSeq, reason);
    } else {
        
    }
}
```

ActivityThread\#performLaunchActivity

```text
private performLaunchActivity() {
    // Activity 信息初始化
    
    // 创建 context
    ContextImpl appContext = createBaseContextForActivity(r);
    Activity activity = null;
    try {
        java.lang.ClassLoader cl = appContext.getClassLoader();
        // 构建 Activity
        activity = mInstrumentation.newActivity(
                    cl, component.getClassName(), r.intent);
    }catch(Exception e){
        
    }
    
    try {
        Application app = r.packageInfo.makeApplication(false, mInstrumentation);
        if(activity != null){
        	activity.attach();
            
            // 通过 Instrumentation 执行 Activity onCreate
            if (r.isPersistable()) {
                 mInstrumentation.callActivityOnCreate(activity, r.state, r.persistentState);
            }else {
                mInstrumentation.callActivityOnCreate(activity, r.state);
            }
            
            if (!r.activity.mFinished) {
                // Activity onStart
            	activity.performStart();
            }
            
            // 通过 Instrumentation 执行 Activity onRestoreInstanceState
            if (!r.activity.mFinished) {
            	if (r.isPersistable()) {
                	if (r.state != null || r.persistentState != null) {
                    	mInstrumentation.callActivityOnRestoreInstanceState(activity, r.state,
                                    r.persistentState);
                    }
                } else if (r.state != null) {
                    mInstrumentation.callActivityOnRestoreInstanceState(activity, r.state);
                }
            }
            
             // 通过 Instrumentation 执行 Activity onPostCreeate
            if (!r.activity.mFinished) {
            	if (r.isPersistable()) {
                    mInstrumentation.callActivityOnPostCreate(activity, r.state,
                                r.persistentState);
                }else {
                    mInstrumentation.callActivityOnPostCreate(activity, r.state);
                }
            }       
    }
        
    return activity;
    
}
```

```text
final void handleResumeActivity() {
    r = performResumeActivity();
    if(r != null) {
        final Activity a = r.activity;
        if (r.window == null && !a.mFinished && willBeVisible) {
            r.window = r.activity.getWindow();
            View decor = r.window.getDecorView();
            decor.setVisibility(View.INVISIBLE);
            ViewManager wm = a.getWindowManager();
            if (a.mVisibleFromClient) {
            	if (!a.mWindowAdded) {
                	a.mWindowAdded = true;
                    // window
                    wm.addView(decor, l);
                }
            }
        }
    }
}
```

> resume 之后才开始添加View

```text
public final ActivityClientRecord performResumeActivity() {
 	if (r != null && !r.activity.mFinished) {
    	try {
            // 处理等待的 Intent
            if (r.pendingIntents != null) {
            	deliverNewIntents(r, r.pendingIntents);
                r.pendingIntents = null;
            }
            // 处理等待的 result
            if (r.pendingResults != null) {
            	deliverResults(r, r.pendingResults);
                r.pendingResults = null;
            }
            // 执行 resume
            r.activity.performResume();
        }
    }
}
```

## Instrumentation

```text
public Activity newActivity(){
    return (Activity)cl.loadClass(className).newInstance();
}
```

```text
private void callActivityOnCreate(){
    prePerformCreate(activity);
    activity.performCreate(icicle);
    postPerformCreate(activity);
}
```

## Activity

```text
final void performCreate() {
    restoreHasCurrentPermissionRequest(icicle);
    // 调用 onCreate
    onCreate(icicle);
    mActivityTransitionState.readState(icicle);
    performCreateCommon();
}
```

```text
final void performStart() {
    mActivityTransitionState.setEnterActivityOptions(this, getActivityOptions());
    mInstrumentation.callActivityOnStart(this);
    mActivityTransitionState.enterReady(this);
}
```

```text
final void performResume() {
    // 执行 restart
    performRestart();
    mInstrumentation.callActivityOnResume(this);
}
```

```text
final void performRestart() {
    if (mStopped) {
        mStopped = false;
        mInstrumentation.callActivityOnRestart(this);
        // 执行 start
        performStart();
    }
}
```

## Instrumentation

```text
public void callActivityOnStart() {
    activity.onStart();
}
```

```text
public void callActivityOnResume() {
     activity.mResumed = true;
    activity.onResume();
}
```

```text
public void callActivityOnRestart() {
    activity.onRestart();
}
```

![Activity &#x542F;&#x52A8;&#x6D41;&#x7A0B;](../../../.gitbook/assets/image%20%2845%29.png)

## 参考

[Activity启动流程\(基于Android26\)](https://juejin.im/entry/5abdcdea51882555784e114d)

[startActivity启动过程分析](http://gityuan.com/2016/03/12/start-activity/)

