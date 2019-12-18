# 事件分发

## Activity/Window/DecorView

### Activity

```text
public boolean dispatchTouchEvent(MotionEvent ev) {
    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
        onUserInteraction();
    }
    if (getWindow().superDispatchTouchEvent(ev)) {
        return true;
    }
    return onTouchEvent(ev);
}
```

### Window

```text
// This is the top-level view of the window, containing the window decor.
private DecorView mDecor;

@Override
public boolean superDispatchTouchEvent(MotionEvent event) {
    return mDecor.superDispatchTouchEvent(event);
}
```

### DecorView

```text
//extends ViewGroup
public class DecorView extends FrameLayout implements RootViewSurfaceTaker, WindowCallbacks{

    public boolean superDispatchTouchEvent(MotionEvent event) {
        //调用 ViewGroup 的 dispatchTouchEvent
        return super.dispatchTouchEvent(event);
    }

}
```

## ViewGroup

```text
@Override
public boolean dispatchTouchEvent(MotionEvent ev) {
    //....
    boolean handled = false;
    final int action = ev.getAction();
    final int actionMasked = action & MotionEvent.ACTION_MASK;

    // Handle an initial down.清除之前状态
    if (actionMasked == MotionEvent.ACTION_DOWN) {
        // Throw away all previous state when starting a new touch gesture.
        // The framework may have dropped the up or cancel event for the previous gesture
        // due to an app switch, ANR, or some other state change.
        cancelAndClearTouchTargets(ev);
        resetTouchState();
    } 
    // 检查是否需要拦截
    final boolean intercepted;
    //如果是down事件或者前面事件已经有子view接收，需要重新判断是否需要拦截
    if (actionMasked == MotionEvent.ACTION_DOWN
            || mFirstTouchTarget != null) {      
        final boolean disallowIntercept = (mGroupFlags & FLAG_DISALLOW_INTERCEPT) != 0;
        if (!disallowIntercept) {
            //拦截没有被禁用时，需要调用 onInterceptTouchEvent 判断
            intercepted = onInterceptTouchEvent(ev);
            ev.setAction(action); // restore action in case it was changed
        } else {
            //禁用了拦截，直接返回false
            intercepted = false;
        }
    } else {
        // 不是down事件，并且也没有其他子view处理过之前的事件
        //则ViewGroup自己拦截处理
        intercepted = true;
    }
    // 检查是否取消
    final boolean canceled = resetCancelNextUpFlag(this)
                || actionMasked == MotionEvent.ACTION_CANCEL;
    
    TouchTarget newTouchTarget = null;
    boolean alreadyDispatchedToNewTouchTarget = false;
    //....
    if (!canceled && !intercepted) {
        if (actionMasked == MotionEvent.ACTION_DOWN
                || (split && actionMasked == MotionEvent.ACTION_POINTER_DOWN)
                || actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
                final int childrenCount = mChildrenCount;
                if (newTouchTarget == null && childrenCount != 0) {
                    final float x = ev.getX(actionIndex);
                    final float y = ev.getY(actionIndex);
                    final ArrayList<View> preorderedList = buildTouchDispatchChildList();
                    final boolean customOrder = preorderedList == null
                                && isChildrenDrawingOrderEnabled();
                    final View[] children = mChildren;
                    for (int i = childrenCount - 1; i >= 0; i--) {
                        final int childIndex = getAndVerifyPreorderedIndex(
                                childrenCount, i, customOrder);
                        final View child = getAndVerifyPreorderedView(
                                preorderedList, children, childIndex);
                        if (!canViewReceivePointerEvents(child)
                                || !isTransformedTouchPointInView(x, y, child, null)) {
                            ev.setTargetAccessibilityFocus(false);
                            continue;
                        }

                        newTouchTarget = getTouchTarget(child);
                        if (newTouchTarget != null) {
                            // Child is already receiving touch within its bounds.
                            // Give it the new pointer in addition to the ones it is handling.
                            newTouchTarget.pointerIdBits |= idBitsToAssign;
                            break;
                        }
                
        }
        
    
    }
    
       

}
```

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

