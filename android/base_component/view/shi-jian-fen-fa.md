# 事件分发



## View的事件分发机制？

事件传递大体过程：Activity--&gt; Window--&gt;DecorView --&gt; View树从上往下，传递过程中谁想拦截就拦截自己处理。MotionEvent是Android中的点击事件。主要事件类型：

* ACTION\_DOWN 手机初次触摸到屏幕事件
* ACTION\_MOVE 手机在屏幕上滑动时触发，会回调多次
* ACTION\_UP 手指离开屏幕时触发

需要关注的几个方法。

* dispatchTouchEvent\(event\);
* onInterceptTouchEvent\(event\);
* onTouchEvent\(event\);

上面3个方法可以用以下伪代码来表示其关系：

```text
public boolean dispatchTouchEvent(MotionEvent ev) {
    boolean consume = false;//事件是否被消费
    if (onInterceptTouchEvent(ev)) {//调用onInterceptTouchEvent判断是否拦截事件
        consume = onTouchEvent(ev);//如果拦截则调用自身的onTouchEvent方法
    } else {
        consume = child.dispatchTouchEvent(ev);//不拦截调用子View的dispatchTouchEvent方法
    }
    return consume;//返回值表示事件是否被消费，true事件终止，false调用父View的onTouchEvent方法
}
```

* * Q：如何解决View的滑动冲突？
  * Q：谈一谈View的工作原理？
  * **onTouch\(\)、onTouchEvent\(\)和onClick\(\)关系？**

    如果一个View需要处理事件，它设置了OnTouchListener，那么OnTouchListener的onTouch方法会被回调。如果onTouch返回false,则onTouchEvent会被调用，反之不会。在onTouchEvent方法中，事件为Action.UP的时候会回调OnClickListener的onClick方法，可见OnClickListener的优先级很低。

