# 事件分发

## ViewRootImpl/DecorView/Activity/Window/DecorView

事件由 ViewRootImpl 分发，会先分发到 DecorView。

```text
//ViewRootImpl.java
private int processPointerEvent(QueuedInputEvent q) {
            final MotionEvent event = (MotionEvent)q.mEvent;
            ...
            //关键点：mView分发Touch事件，mView就是DecorView
            boolean handled = mView.dispatchPointerEvent(event);
            maybeUpdatePointerIcon(event);
            maybeUpdateTooltip(event);
            ...
       }
```

```text
 // View.java
 public final boolean dispatchPointerEvent(MotionEvent event) {
        if (event.isTouchEvent()) {
            //分发Touch事件
            return dispatchTouchEvent(event);
        } else {
            return dispatchGenericMotionEvent(event);
        }
    }
```

```text
//FrameLayout
@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final Window.Callback cb = mWindow.getCallback();
        return cb != null && !mWindow.isDestroyed() && mFeatureId < 0
                ? cb.dispatchTouchEvent(ev) : super.dispatchTouchEvent(ev);
    }
```

`Window.Callback`都被Activity和Dialog实现，所以变量cb可能就是Activity和Dialog。

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

    // 如果是 down 事件，清除之前状态
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
        //不是down事件，并且也没有其他子view处理过之前的事件
        //则ViewGroup自己拦截处理
        intercepted = true;
    }
    // 检查是否取消
    final boolean canceled = resetCancelNextUpFlag(this)
                || actionMasked == MotionEvent.ACTION_CANCEL;
    
    TouchTarget newTouchTarget = null;
    //是否已由新目标处理事件
    boolean alreadyDispatchedToNewTouchTarget = false;
    //....
    
    if (!canceled && !intercepted) {
        //只针对这三种事件寻找View，其他情况一律由之前的目标处理
        if (actionMasked == MotionEvent.ACTION_DOWN
                || (split && actionMasked == MotionEvent.ACTION_POINTER_DOWN)
                || actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
                
                final int childrenCount = mChildrenCount;
                if (newTouchTarget == null && childrenCount != 0) {
                    final float x = ev.getX(actionIndex);
                    final float y = ev.getY(actionIndex);
                    //按照z轴坐标以及自定义绘制顺序（如果有的话）排列子view
                    final ArrayList<View> preorderedList = buildTouchDispatchChildList();
                    final boolean customOrder = preorderedList == null
                                && isChildrenDrawingOrderEnabled();
                    final View[] children = mChildren;
                    for (int i = childrenCount - 1; i >= 0; i--) {
                        //获取view在列表中的索引
                        final int childIndex = getAndVerifyPreorderedIndex(
                                childrenCount, i, customOrder);
                        final View child = getAndVerifyPreorderedView(
                                preorderedList, children, childIndex);
                        //检查事件发生位置是否在View内以及View是否可以接受事件        
                        if (!canViewReceivePointerEvents(child)
                                || !isTransformedTouchPointInView(x, y, child, null)) {
                            ev.setTargetAccessibilityFocus(false);
                            continue;
                        }
                        
                        //此时找到了事件发生在区域内且可以接收事件的view

                        //在 touchTarget 链表中寻找 View对应的TouchTarget
                        newTouchTarget = getTouchTarget(child);
                        if (newTouchTarget != null) {
                            //view 已经在接受事件了
                            // Child is already receiving touch within its bounds.
                            // Give it the new pointer in addition to the ones it is handling.
                            newTouchTarget.pointerIdBits |= idBitsToAssign;
                            break;
                        }
                        //没有找到对应target，先分发事件给 view
                        if (dispatchTransformedTouchEvent(ev, false, child, idBitsToAssign)) {
                            // View 的 dispatchTouchEvent 返回了true
                            mLastTouchDownTime = ev.getDownTime();
                            if (preorderedList != null) {
                                // 找到在mChildren中的真实索引
                                for (int j = 0; j < childrenCount; j++) {
                                    if (children[childIndex] == mChildren[j]) {
                                        mLastTouchDownIndex = j;
                                        break;
                                    }
                                }
                            } else {
                                mLastTouchDownIndex = childIndex;
                            }
                            mLastTouchDownX = ev.getX();
                            mLastTouchDownY = ev.getY();
                            //添加新的TouchTarget
                            newTouchTarget = addTouchTarget(child, idBitsToAssign);
                            alreadyDispatchedToNewTouchTarget = true;
                            break;
                         }
                     }//end for
                     if (preorderedList != null) preorderedList.clear();
                 }//end if
                 if (newTouchTarget == null && mFirstTouchTarget != null) {
                    // Did not find a child to receive the event.
                    // Assign the pointer to the least recently added target.
                    newTouchTarget = mFirstTouchTarget;
                    while (newTouchTarget.next != null) {
                        newTouchTarget = newTouchTarget.next;
                    }
                    newTouchTarget.pointerIdBits |= idBitsToAssign;
                }
            }//end if
            
            // Dispatch to touch targets.
            if (mFirstTouchTarget == null) {
                //调用父类的 dispatchToucheEvent
                handled = dispatchTransformedTouchEvent(ev, canceled, null,
                        TouchTarget.ALL_POINTER_IDS);
            } else {
                // Dispatch to touch targets, excluding the new touch target if we already
                // dispatched to it.  Cancel touch targets if necessary.
                TouchTarget predecessor = null;
                TouchTarget target = mFirstTouchTarget;
                while (target != null) {
                    final TouchTarget next = target.next;
                    if (alreadyDispatchedToNewTouchTarget && target == newTouchTarget) {
                        //Down 事件时走到这里
                        handled = true;
                    } else {
                        final boolean cancelChild = resetCancelNextUpFlag(target.child)
                                || intercepted;
                        //其他事件走到这里        
                        if (dispatchTransformedTouchEvent(ev, cancelChild,
                                target.child, target.pointerIdBits)) {
                            handled = true;
                        }
                        if (cancelChild) {
                            if (predecessor == null) {
                                mFirstTouchTarget = next;
                            } else {
                                predecessor.next = next;
                            }
                            target.recycle();
                            target = next;
                            continue;
                        }
                    }
                    predecessor = target;
                    target = next;
                }//end while
            }//end else                              
        }//end if    
    return handled; 
}
```

## View

```text
public boolean dispatchTouchEvent(MotionEvent event) {
    boolean result = false;
    if (onFilterTouchEventForSecurity(event)) {
        if ((mViewFlags & ENABLED_MASK) == ENABLED && handleScrollBarDragging(event)) {
            result = true;
        }
        
        ListenerInfo li = mListenerInfo;
        if (li != null && li.mOnTouchListener != null
                && (mViewFlags & ENABLED_MASK) == ENABLED
                && li.mOnTouchListener.onTouch(this, event)) {
            //设置了onTouchListener/enable/onTouch返回true
            //直接返回
            result = true;
        }

        if (!result && onTouchEvent(event)) {
            result = true;
        }
    }
    return result;
}
```

onTouchEvent\(event\) 中会调用onClick 和 onLongClick。

调用onClick 是在UP事件时，检查是否clickable以及是否设置监听；

调用onLongClick 是在Down 事件时，发出一个延时任务，如果任务执行时还是按下状态，就执行难onLongClick.

//todo 源码

## 多点触控

处理POINTER\_DOWN 和POINTER\_UP，进行指针处理。

区分 actionIndex\(pointerIndex\)/ pointerId.

##  滑动冲突

父类根据需要重写 onIntercept 或者子View根据需要调用requestDisallowIntercept.

## 相关问题

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

### 如何解决View的滑动冲突？

父类根据需要重写 onIntercept 或者子View根据需要调用requestDisallowIntercept

### onTouch\(\)、onTouchEvent\(\)和onClick\(\)关系？

如果一个View需要处理事件，它设置了OnTouchListener，那么OnTouchListener的onTouch方法会被回调。如果onTouch返回false,则onTouchEvent会被调用，反之不会。在onTouchEvent方法中，事件为Action.UP的时候会回调OnClickListener的onClick方法，可见OnClickListener的优先级很低。

### 防止短时间内重复点击

通过在对比两次`ACTION_DOWN`事件之间的时间间隔是否小于最小间隔，如果小于直接忽略。

视应用范围，这段逻辑可以放在View/ViewGroup/Activity 中 。

```text
private static final long CLICK_DURATION = 900;

//上次 ACTION_DOWN 事件发生时间
private long lastDownTime = 0;

@Override
public boolean dispatchTouchEvent(MotionEvent event) {
    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
        boolean ignore = event.getDownTime() - lastDownTime < CLICK_DURATION;
        lastDownTime = event.getDownTime();
        if (ignore) {
            return false;
        }
    }
    return super.dispatchTouchEvent(event);
}
```

## 相关链接

[安卓自定义View进阶-MotionEvent详解](https://www.gcssloop.com/customview/motionevent)

[玩安卓\|每日一问 ](https://www.wanandroid.com/wenda/show/11287)

[玩安卓\|每日一问：多指触控](https://www.wanandroid.com/wenda/show/10049)

[安卓自定义View进阶-多点触控详解](https://www.gcssloop.com/customview/multi-touch)





