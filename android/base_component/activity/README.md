# Activity

## 

Activity的启动过程？

1. Activity startActivityForResult
2. Instrumentation execStartActivity
3. AMS startActivity
4. ApplicationThread scheduleLaunchActivity
5. ActivityThread.H handleMessage -&gt; performLaunchActivity
6. Activity attach
7. Instrumentation callActivityOnCreate

