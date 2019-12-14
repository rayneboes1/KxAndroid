# Activity

## Activity 的生命周期

### onStart\(\)和onResume\(\)的区别？

### onPause\(\)和onStop\(\)的区别？

### Activity A启动另一个Activity B会回调哪些方法？如果Activity B是完全透明呢？如果启动的是一个Dialog呢？

### 谈谈onSaveInstanceState\(\)方法？何时会调用？

### onSaveInstanceState\(\)与onPause\(\)的区别？

## 如何避免配置改变时Activity重建？

## 优先级低的Activity在内存不足被回收后怎样做可以恢复到销毁前状态？

## 启动模式

* Q：说下Activity的四种启动模式？（有时会出个实际问题来分析返回栈中Activity的情况）

### 谈谈singleTop和singleTask的区别以及应用场景

### onNewIntent\(\)调用时机？

### 了解哪些Activity启动模式的标记位？

## 如何启动其他应用的Activity

## Activity的启动过程？

1. Activity startActivityForResult
2. Instrumentation execStartActivity
3. AMS startActivity
4. ApplicationThread scheduleLaunchActivity
5. ActivityThread.H handleMessage -&gt; performLaunchActivity
6. Activity attach
7. Instrumentation callActivityOnCreate

