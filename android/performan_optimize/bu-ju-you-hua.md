# 布局优化

* 如果父控件有颜色，也是自己需要的颜色，那么就不必在子控件加背景颜色
* 如果子控件有背景颜色，并且能完全覆盖父控件，那么父控件不用设置背景颜色
* 尽量减少不必要的嵌套
* 能用LinearLayout和FrameLayout，就不要用RelativeLayout，因为RelativeLayout相对比较复杂，测绘也相对耗时。
* include和merge一起使用，增加复用，减少层级
* ViewStub按需加载，更加轻便
* 复杂界面选择ConstraintLayout，可有效减少层级



