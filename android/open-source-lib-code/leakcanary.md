# LeakCanary

## 原理

```text
public static void main(String[] args) {
        ReferenceQueue<Object> q = new ReferenceQueue<>();
        Object test = new Object();
        WeakReference<Object> weakReference = new WeakReference<>(test,q);
        System.out.println("是否在队列中？"+(q.poll()==weakReference));
        test=null;
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Runtime.getRuntime().gc();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("是否在队列中？"+(q.poll()==weakReference));

    }
```

### Activity

通过 Application 注册 ActivityLifeCycleCallback，当Activity onDestroy 时，开始监测Activity。

具体方式时创建指向Activity的 WeakReference，并指定一个 ReferenceQueue。当Activity被回收时，WeakReference 会被添加到引用队列中。通过强制执行垃圾回收，确保Activity在未泄漏的情况下能被正常回收。然后去ReferenceQueue 中去找Activity对应的 WeakReference，如果没有，说明发生了泄漏。

### Fragment

通过 FragmentManager\#registerFragmentLifecycleCallbacks，注册Fragment生命周期callback，然后在 Fragment destroy或者viewDestroy时，检查Fragment或者 Fragment的view是否泄漏。

## 相关链接

[深入理解 Android 之 LeakCanary 源码解析](https://allenwu.itscoder.com/leakcanary-source)

