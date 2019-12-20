# 应用架构

## MVC

在Android中View和Controller一般就是被Activity充当了，当逻辑非常多，操作非常复杂时，Activity代码量非常庞大，不易维护。

* Model : 模型层，业务逻辑+数据存储等
* View : 用户界面，一般就是xml+Activity
* Controller : 控制层，一般就是Activity

## MVP

我个人角度，现在\(2019年10月29日20:02:49\)大多是使用这种方式，既不复杂也解耦合了。

* Model：模型层，业务逻辑+数据存储+网络请求
* View：视图层，View绘制和用户交互等，一般是Activity
* Presenter：呈现层，连接V层和M层，完成他们之间的交互

接口过多，对增加和修改很不友好

## MVVM

为了更加分离M，V层，所以有了MVVM。

* Model：模型层，业务逻辑+数据存储+网络请求
* View：视图层，View绘制和用户交互等，一般是Activity
* ViewModel：其实就是Presenter和View的数据模型的合体。**双向绑定**，View的变动会反应到ViewModel中，数据的变动也会反应到View上。

## 组件化的好处

1. 任意修改都需要编译整个工程，效率低下，组件化可以单独编译工程。
2. 解耦，有利于多人团队协作开发
3. 功能复用

[Android 组件化最佳实践](https://juejin.im/post/5b5f17976fb9a04fa775658d)

