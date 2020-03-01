# 启动模式与任务栈

## Activity 的四种启动模式？

| 启动模式 | 作用 |
| :---: | :--- |
| standard | 默认启动模式，每次启动都会创建新的Activity实例，并放到栈顶 |
| singleTop | 栈顶复用，如果要启动的Activity在栈顶已经有实例，则复用这个实例并调用 onNewIntent方法 |
| singleTask | 栈内复用，如果Activity所指定的任务栈\(可在清单文件中通过 taskAffinity 指定\)中已经有实例，则复用这个实例，并将此实例上面的Activity全部出栈，将此Activity置于栈顶 |
| singleInstance | 单例复用，只会创建一次，并且Activity实例处于单独的任务栈中，且是任务栈中唯一的 Activity。 |

## 谈谈 `singleTop` 和 `singleTask` 的区别以及应用场景

singleTop 是当要启动的Activity 在当前任务栈顶有实例时会复用并调用onNewIntent方法，适用于重复事件的落地页，比如通知详情。

singTask 单独任务栈，比如浏览器。

## onNewIntent\(\)调用时机？

Activity 实例复用时调用。

## 了解哪些 Activity 启动模式的标记位？

### 通过 AndroidManifest 设置

* standard
* singleTop
* singleTask
* singleInstance

### 通过 Intent.addFlags\(\) 设置

* FLAG\_ACTIVITY\_SINGLE\_TOP:同 singleTop
* FLAG\_ACTIVITY\_NEW\_TASK：同 singleTask
* FLAG\_ACTIVITY\_CLEAR\_TOP：将 Activity 上面的其他 Activity 出栈,如果同时指定

  FLAG\_ACTIVITY\_SINGLE\_TOP 则会复用，否则会重建

* FLAG\_ACTIVITY\_EXCLUDE\_FROM\_RECENTS：具有该标记的Activity不会出现在历史Activity的列表中，即无法通过历史列表回到该Activity上
* FLAG\_ACTIVITY\_NO\_ANIMATION：不使用动画
* ....

**通过Intent 设置的启动模式标记位优先于清单中设置的标记位。**

## 相关链接

[官方文档](https://developer.android.google.cn/guide/components/activities/tasks-and-back-stack)



