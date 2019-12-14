# View

## View

* * Q：MotionEvent是什么？包含几种事件？什么条件下会产生？
  * Q：scrollTo\(\)和scrollBy\(\)的区别？
  * Q：Scroller中最重要的两个方法是什么？主要目的是？

### LinearLayout 测量

先做一次测量，做完之后有空间剩余，有weight的View再测量一下，分一下剩余的空间。





## invalidate\(\)和postInvalidate\(\)的区别？

## Android 换肤

重新设置LayoutInflater的Factory2，从而拦截创建View的过程，然后搞成自己的控件，想怎么换肤就怎么换肤。

## 

## View 与 SurfaceView

* View是Android中所有控件的基类
* View适用于主动更新的情况，而SurfaceView则适用于被动更新的情况，比如频繁刷新界面。
* View在主线程中对页面进行刷新，而SurfaceView则开启一个子线程来对页面进行刷新。
* View在绘图时没有实现双缓冲机制，SurfaceView在底层机制中就实现了双缓冲机制。

