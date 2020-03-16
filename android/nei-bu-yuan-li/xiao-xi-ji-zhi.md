# 消息循环机制

## 消息循环流程

Android 消息循环主要涉及Looper、Handler、MessageQueue和Message 四个类，他们之间的关系如下图所示：

![Android &#x6D88;&#x606F;&#x673A;&#x5236;&#x56FE;](../../.gitbook/assets/image%20%2826%29.png)

简单总结就是：`Message` 由 `Handler` 发送，按照发生时间升序保存在 `MessageQueue` 中，然后 `Looper` 不断从 `MessageQueue` 中取出下一条待处理的 `Message` ，并通知发送它的 `Handler` 进行处理。 

## 各个类的作用

### Message

代表一个消息（或事件），可以携带数据，持有发送它的`Handler`的引用。

### MessageQueue

消息队列，以单链表的形式按照发生时间升序排列 Message。

提供了 `enqueueMessage()`用于把新消息插入到MessageQueue中，也提供了`next()`方法用于获取下一条待处理消息。（**当队列无消息或者消息处理时间未到时，会阻塞等待。**）

### Looper

消息循环的入口，提供 `Looper.prepare()` 用于创建 `Looper` 线程，以及 `Looper.loop()`方法进入消息循环，持有 MessageQueue 的引用。

Looper.prepare\(\) 方法用于给当前线程设置Looper,通过 [ThreadLocal](../../java/concurrent/threadlocal.md) 将当前线程和它的 Looper 进行绑定，每个线程只能有一个Looper。

`Looper.loop()` 方法就是通过 `MessageQueue`的`next()`方法获取下一条待处理消息，然后通知消息的 `Handler` 处理消息。

```text
public static void loop() {
    //获取当前线程的 looper
    final Looper me = myLooper();
    //如果在调用 loop 之前没有调用 prepare,就会抛出一个异常
    if (me == null) {
        throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
    }
    //获取 Looper 对应的 MessageQueue
    final MessageQueue queue = me.mQueue;

    for (;;) {
        //通过 MessageQueue 的 next 方法获取下一个消息
        //在没有消息、或消息处理时间未到时可能会阻塞
        Message msg = queue.next();

        //消息队列为空，退出循环
        if (msg == null) {
            return;
        }

        try {
            //通知 msg 对应的 Handler 处理消息
            msg.target.dispatchMessage(msg);
        } finally {
            //...
        }
        //...

        //回收 msg
        msg.recycleUnchecked();
    }
}
```

### Handler

用于发送和处理消息。提供了一系列 send 方法来将 `Message` 插入到 MessageQueue中，最终调用 enqueueMessage 方法。

```text
private boolean enqueueMessage(MessageQueue queue, Message msg, long uptimeMillis) {
    //给 msg 的 target 赋值为当前这个 Handler
    msg.target = this;
    //如果 Handler 是否异步来设置 Message
    if (mAsynchronous) {
        msg.setAsynchronous(true);
    }
    //调用 MessageQueue 的 enqueueMessage
    return queue.enqueueMessage(msg, uptimeMillis);
}
```

 提供了 dispatchMessage\(Message msg\) 方法用于处理消息，具体逻辑如下：

![Handler &#x7684; dispatchMessage &#x6D41;&#x7A0B;](../../.gitbook/assets/image%20%287%29.png)

对应源码：

```text
public void dispatchMessage(Message msg) {
    if (msg.callback != null) {
        handleCallback(msg);
    } else {
        if (mCallback != null) {
            if (mCallback.handleMessage(msg)) {
                return;
            }
        }
        handleMessage(msg);
    }
}
```

### 各个类和方法调用关系图

![&#x6D88;&#x606F;&#x5FAA;&#x73AF;&#x6D89;&#x53CA;&#x7684;&#x7C7B;&#x548C;&#x65B9;&#x6CD5;&#x4EE5;&#x53CA;&#x8C03;&#x7528;&#x5173;&#x7CFB;](../../.gitbook/assets/image%20%2842%29.png)

## 主线程 Looper 设置

## Looper 线程的等待和唤醒（JNI 层）

### MessageQueue 的 nativeInit 方法

MessageQueue 的构造方法中调用了 nativeInit 方法，这个方法在JNI层创建了一个 NativeMessageQueue ，并将它保存在 Java 层 MessageQueue 的 `mPrt` 变量中。

在创建 NativeMessageQueue 过程中，也在 Native 层创建了一个 Looper，Looper 内部创建了一个管道，用于控制线程的等待和唤醒。当管道中有内容可读时，就唤醒当前正在等待管道中的内容的线程。

> 管道就是一个文件，在管道的两端，分别是两个打开文件文件描述符，这两个打开文件描述符都是对应同一个文件，其中一个是用来读的，别一个是用来写的，一般的使用方式就是，一个线程通过读文件描述符中来读管道的内容，当管道没有内容时，这个线程就会进入等待状态，而另外一个线程通过写文件描述符来向管道中写入内容，写入内容的时候，如果另一端正有线程正在等待管道中的内容，那么这个线程就会被唤醒。**这里借助了 Linux 系统的epoll 机制。**

### MessageQueue 的 next 方法

```text
Message next() {
    //...
    //用于 native 层的变量
    final long ptr = mPtr;
    if (ptr == 0) {
        return null;
    }
    //取下一条消息前需要等待的时间
    int nextPollTimeoutMillis = 0;
    for (;;) {
        //...
        
        //查看是否有消息
        nativePollOnce(ptr, nextPollTimeoutMillis);
        synchronized (this) {
            //获取当前时间
            final long now = SystemClock.uptimeMillis();           
            Message prevMsg = null;
            Message msg = mMessages;          
            //...
            if (msg != null) {
                if (now < msg.when) {
                    // 下一条消息分发时间比当前晚，设定等待时间
                    nextPollTimeoutMillis = (int) Math.min(msg.when - now, Integer.MAX_VALUE);
                } else {
                    // 取出下一条msg
                    mBlocked = false;
                    if (prevMsg != null) {
                        prevMsg.next = msg.next;
                    } else {
                        mMessages = msg.next;
                    }
                    msg.next = null;
                    msg.markInUse();
                    return msg;
                }
            } else {
                // 队列中已没有更多消息
                nextPollTimeoutMillis = -1;
            }
            //....
        }//end sychronized
    }//end for
}
```

主要的方法是 `nativePollOnce(mPtr, nextPollTimeoutMillis)`，这个方法会调用NativeMessageQueue 的 pollOnce 方法，实际调用的是 Native 层的 Looper 的 pullOnce 方法，

```text
int Looper::pollOnce(int timeoutMillis, int* outFd, int* outEvents, void** outData) {
	int result = 0;
	for (;;) {
		......
 
		if (result != 0) {
			......
 
			return result;
		}
 
		result = pollInner(timeoutMillis);
	}
}
。
原文链接：https://blog.csdn.net/luoshengyang/article/details/6817933
```

pollOnce 主要通过 pollInner 的返回值来判断是否需要返回。pollInner 方法通过 **epoll\_wait** 来检查管道是否有写入事件，如果有或者等待超时，那么就会返回；**否则就会在epoll\_wait中进入睡眠。**

如果 epoll\_wait 返回值是 0，即**等待超时**；如果大于0，则说明有写入事件发生，这时会清空管道内容并返回。

### MessageQueue 的 enqueueMessage

```text
final boolean enqueueMessage(Message msg, long when) {
		......
 
		final boolean needWake;
		synchronized (this) {
			......
 
			msg.when = when;
			Message p = mMessages;
			if (p == null || when == 0 || when < p.when) {
				msg.next = p;
				mMessages = msg;
				needWake = mBlocked; 
			} else {
				Message prev = null;
				while (p != null && p.when <= when) {
					prev = p;
					p = p.next;
				}
				msg.next = prev.next;
				prev.next = msg;
				needWake = false; // still waiting on head, no need to wake up
			}
 
		}
		if (needWake) {
			nativeWake(mPtr);
		}
		return true;

————————————————
版权声明：本文为CSDN博主「罗升阳」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/luoshengyang/article/details/6817933
```

在将消息放入消息队列后，根据情况判断是否需要唤醒。如果消息队列不为空，那么就不需要唤醒；**如果消息队列为空，那么此时线程还在epoll\_wait上空闲等待，就需要进行唤醒**。

唤醒调用了native方法 nativeWake，这个调用NativeMessageQueue的wake方法，进而调用Native 层Looper 的wake方法。Native Looper 的wake 就是往管道中写入一个"W"字符串，这时在管道读的一端 epoll\_wait 上等待的线程就会被唤醒，从而在Java 层MessageQueue 的next方法中调用的 `nativePollOnce`返回，此时消息队列中已经放入了新的消息，程序就会拿到消息队列的消息进行处理或等待。

### 总结

Looper 线程的等待和唤醒是在 native 层通过**管道**实现的。

在创建 MessageQueue 的时候在 Native 层也创建了对应的 NativeMessageQueue 和  Looper。NativeMessageQueue 主要用于Java 层的 MessageQueue 调用，保存在它的 mPtr 变量中。而 Native 的 Looper 则是基于管道和 epoll 机制来实现等待和唤醒的。 

当通过 MessageQueue 的 next 方法获取下一条消息时，会检查是否需要等待，如果需要，就调用 epoll\_wait 等待管道的写入事件，直到超时或者有写入事件时会被唤醒。

当通过 MessageQueue 的 enqueueMessage 插入消息时，如果此时线程处于空闲等待中，则会进行唤醒。唤醒通过 Native 的 Looper 的 wake 方法，向管道中写入一个“W”字符，此时正在等待的线程就回被唤醒，nativeOncePoll 方法返回，程序开始检查消息队列中的消息决定是否处理。



## IdleHanlder

```text
/**
* Callback interface for discovering when a thread is going to block
* waiting for more messages.
*/
public static interface IdleHandler {
    /**
     * Called when the message queue has run out of messages and will now
     * wait for more.  Return true to keep your idle handler active, false
     * to have it removed.  This may be called if there are still messages
     * pending in the queue, but they are all scheduled to be dispatched
     * after the current time.
     */
    boolean queueIdle();
}
```

调用MessageQueue\#next\(\) 方法且第一次循环消息队列为空或者有消息但是时间没到时执行。



## 相关问题

### 为什么系统不建议在子线程访问UI？

* View 并不是线程安全的，多线程访问可能使界面状态不可预测
* 允许多线程需要额外的同步工作，开发成本高且性能不好（借助锁）

### 一个Thread可以有几个Looper？几个Handler？

一个Thread 只能有一个 Looper ，如果重复调用Looper的prepare方法，会报错。

```text
private static void prepare(boolean quitAllowed) {
    if (sThreadLocal.get() != null) {
        throw new RuntimeException("Only one Looper may be created per thread");
    }
    sThreadLocal.set(new Looper(quitAllowed));
}
```

一个 LooperThread 可以有多个Handler。

### 可以在子线程直接new一个Handler吗？那该怎么做？

不可以。创建Handler时会通过当前线程的Looper来初始化内部变量和MessageQueue，如果线程还没有对应的Looper则会抛出异常。

先调用Looper.prepare\(\)方法，然后再创建Handler，最后通过Looper.loop\(\)进入消息循环。

### Message可以如何创建？哪种效果更好，为什么？

通过 Message 的 obtain 方法创建更改，Message 类内部有静态缓存池，会缓存之前已经分发并处理过的消息对象。这样可以实现对象复用，避免多次创建对象带来的内存开销和垃圾回收。

### 这里的 ThreadLocal 有什么作用？

将当前线程及其对应的 Looper 进行映射，方便直接通过 `Looper.myLooper()`获取当前线程对应的 Looper 对象。 

### 主线程中 Looper 的轮询死循环为何没有阻塞主线程？

如果消息队列中没有消息或者当前消息的处理时间未到，此时线程会在native 的epoll\_wait函数中空闲等待，不会占用CPU资源。

当其他线程发送消息或者等待超时后，主线程被唤醒，开始检查消息队列中的消息是否需要处理。

## 链接

* [Android 消息机制原理解析](https://wenhaiz.xyz/handler-in-android)
* [Android应用程序消息处理机制（Looper、Handler）分析](https://blog.csdn.net/luoshengyang/article/details/6817933)





