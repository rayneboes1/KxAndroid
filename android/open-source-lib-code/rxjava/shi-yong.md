# 使用笔记

## **啥是响应式编程**

使用可观察流进行的异步编程（Asynchronous programing with observable streams）

* 异步编程
  * 耗时操作在主线程之外执行
  * 事件驱动，事件本身是异步的并且随时都有可能发生
* 可观察 基于观察者模式，可由多个观察者同时订阅，在状态发生变化时，通知所有观察者
* 流 事件序列
* 可观察流 可以订阅的事件序列
* 响应式编程 基于推送、观察者模式来异步处理数据流的方法
* 数据是第一公民 程序可以看做由数据驱动，而不是线程按顺序执行

### RxJava 特点

* 基于观察者模式
* 改造迭代器模式，使数据基于推送传递给观察者
* 函数式编程
* 简洁 借助 Lambda
* 链式调用
* 支持懒加载
* 实现并发简单 切换线程十分方便
* 异步错误处理 观察者可以进行错误处理

## **RxJava 核心组件**

### 3 O

Observable,Observer and Operator

#### Observale 数据流，可观察对象

* 提供了多个重载的 subscibe 方法用于注册 Observer
* 虽然基于推送（回调），但并不是异步的。默认情况下，Observable 是同步的，事件将会在 subscribe\(\) 执行的线程产生
* Hot or Cold
  * Cold Observale 在每个观察者订阅时，会重新产生一遍数据
  * Hot Observable 则会按照顺序产生事件，观察者只能收到自订阅后产生的事件
* 创建 Observable RxJava 提供了一系列接口用来创建 Observable
  * Observable.just\(\)：将出入的参数组装成一个 Observable 支持 1 到 9 个数据
  * Observable.fromArray\(\)/Observable.fromIterable\(\) 通过数组或可迭代集合创建一个 Observable
  * Observable.range\(\)/Observable.rangeLong\(\) 通过一个区间创建产生整数或 long 类型的 Observable
  * Observable.empty\(\) 不产生数据，只调用 onCompelte\(\)
  * Observable.error\(\) 只调用 onError\(\)
  * Observable.never\(\) 不调用 Observer 的任何方法，一般用于测试
  * **Observable.create\(\)** 更加灵活和基础的创建方法，可以根据需要调用 Observer 的方法
  * 懒发射
    * 以下两个创建方法中，参数提供的操作只会在订阅后才开始执行
    * Observable.fromCallable\(\) 参数提供的函数返回要发射的数据
    * Observable.defer\(\) 参数提供的函数返回一个 Observable（当 Observable 本身的创建也很耗时的时候，用 defer\(\) 比较合适）

#### Observer 观察者，接收 Observable 发出的数据。当 onComplete\(\)和 onError 回调后，就不会再产生事件

* 事件回调
  * onSubscribe\(Disposable d\) 订阅成功后回调,Disposable 可以用来取消订阅
  * onNext\(T value\) 每当当 Observable 产生一个事件，都会回调
  * onComplete\(\) Observable 不再产生数据时回调
  * onError\(Throwable e\) Observable 内部出错时回调

#### Operator

对数据进行一系列转换

* 支持链式调用
* 提高可读性
* 让开发者更关注做什么而不是怎么做
* onNext\(\) 接收到的数据最好是最终数据，转换都应该放在 Operator 中

## **Operators**

> 不要在 Operator 中修改原数据

* 变换与过滤
  * map:将一个类型转换成另外一种类型
  * filter：将不符合条件的数据过滤掉
* FlatMap
  * 传入的函数需要返回一个 Observable
  * 返回的 Observable 会立刻被订阅，并且产生的数据将被合并发送到 Observer
  * 如果有延迟，flatMap 产生的序列 可能和 Observable 的产生顺序不一致
* ConcatMap
  * 与 FlatMap 类似，不过会等前一个 Observable 产生完数据，才会订阅下一个，因此不会出现数据交叉的情况
* Skip：忽略前面 n 个数据
* Take：只取前面 n 个数据
* Fisrt&Last：只取第一个或最后一个数据 返回一个特殊的 Observable：Single（只产生一个数据）last 等所有数据产生完成后才会收到数据​
* 组合
  * Observable.merge\(\):将多个 Observable 组合成一个，数据可能会交叉产生，当其中一个 Observable 调用 onComplete\(\)后，合并后的 Observable 就不再产生数据
  * Observable.concat\(\):多个 Observable 按顺序订阅，前一个产生完数据后才会订阅后一个，因此产生数据不会交叉，所有 Observable 都完成后才会结束
  * Observable.zip\(\):将多个 Observable 的数据按产生顺序组合成一个新的 Observable 序列，新 Observable 序列的长度等于包含数据最少的 Observable 的长度 如果一个 Observable 产生数据过快 ，那么会缓存产生过快的 Observable 数据并等待较慢的那个
* 聚合
  * toList\(\):将一个 Observable 的数据打包成一个 List，并返回 Observable&lt;List&lt;T&gt;&gt; 调用 onComplete\(\)后才会产生 toList\(\),如果没有调用onComplete ，那么 toList 也不会被调用
  * reduce\(\):将元素累积，形成一个最终结果 比如累加。不能用于将数据累积到一个可变的数据结构中
  * collect\(\):用于将元素累积到一个集合中,需要传一个 Callable 用于创建一个集合，然后再把数据累积到集合中 比如将字符拼接到一个 StringBuilder
* 工具性 Operator
  * doOnEach\(\):每产生一个事件就会被回调（包括 onError onComplete）
  * donOnNext\(\) doOnError\(\) doOnComplete\(\)
* Cache:用于缓存Observable 产生的事件 可以配合 timestamp\(\) 来为缓存事件加上时间戳，用于判断是否需要重新产生事件
* 重用操作链
  * **将一系列操作链封装到ObservableTransformer 中，然后通过 Observable.compose\(\)方法传入**

\*\*\*\*

## **多线程**

Scheduler 的使用

* RxJava 默认是同步的
* Schedulers
  * Schedulers.newThread\(\):新建一个线程
  * Schedulers.single\(\):单线程，任务按顺序执行
  * Schedulers.from\(Executor\):基于提供的 Executor
  * [Schedulers.io](http://schedulers.io/)\(\):主要用于 IO 操作，线程数会随着任务适当增加
  * Schedulers.computation\(\):用于 CPU 密集型任务，使用等于 CPU 核数的线程（因为创建多个线程没有意义，反而会消耗性能）
  * Schedulers.trampoline\(\):在当前线程将任务组织成队列执行
* Observable.subscribeOn\(\):控制 Observable 的创建过程在哪个 Scheduler 执行
* Observable.observeOn\(\)：控制观察者的行为在哪个 Scheduler 执行 RxAndroid 提供了 Android 平台的线程模型，比如 AnroidSchduler.mainThread\(\)
* 细节
  * 链式调用中，只有第一个 subscribeOn 会生效，后续的都不会起作用 如果不想调用者修改Observable 的创建执行线程，可以先指定线程
  * observeOn 可以被调用多次，每次都会影响后面的操作 应该确保 observeOn 尽可能靠近 subscribe 方法，以确保计算操作尽肯能少的出现在主线程
* 使用 FlatMap 进行并发：在创建 Observable 的时候通过 subscribeOn 指定执行线程 不能保证顺序

## **Reactive Modeling on Android**

* 非响应式方法 RxJava 提供的用来将 Observable 转换成非响应式的方法，应尽量避免使用
  * blockingFirst\(\)：阻塞调用线程，直到 Observable 产生第一个元素
  * blockingLast\(\)：阻塞调用线程，直到 Observable 产生最后一个元素
  * blockingNext\(\)：返回一个 Iterable 对象，可以用来遍历 Observable 产生的所有数据，每遍历一个数据都会阻塞调用线程
  * blockingSingle\(\)：阻塞调用线程，直到 Observable 产生一个元素以及一个后续的 onComplete 事件，否则会抛出异常
  * blockingSubscribe\(\)：阻塞调用线程，直到 Observable 产生一个终止事件（onNext 被忽略）
* 懒加载 耗时任务应该在订阅后才开始执行
  * 使用 defer 或者 fromCallable 来包装创建 Observable 的操作即可
* Reactive Everything
  * 处理耗时操作 网络请求、文件读写、Bitmap 处理等
    * 将耗时操作包装到 Observable 中，并制定执行线程，这样调用端就不用关心该操作是否会阻塞主线程
    * Completable、Single 和 Maybe 几种特殊的 Observable
      * Single：只产生一个元素
        * 对应 SingleObserver 只有三个回调：onSubscribe\(Disposable d\) 、onSuccess\(T value\) 和 onError\(Throwable t\)
      * Completable：不产生元素，只通知操作是否成功
        * 对应 CompletableObserver 回调：onSubscribe\(Disposable d\) 、onComplete\(\) 和 onError\(Throwable t\)
      * Maybe:可能产生一个元素，也可能没有元素，可以看做 Single 和 Completable 的组合
        * 对应 MaybeObserver 回调：
          * onSubscribe\(Disposable d\)
          * onSuccess\(T value\)
          * onComplete\(\)：没有产生元素时回调
          * onError\(Throwable t\)
      * 与 Observable 相互转换
        * Completable、Single 和 Maybe 有 toObservable 方法
        * Observable 的 toList 方法可以转换成 Single
  * 替换回调 通过 Observable.create\(\)方法，创建 Observable 并调用观察者的一系列方法来更新数据，替换常用的回调形式编码
    * 多点传播
      * Cold Observable to Hot Observable create\(\) 方法返回的是一个 Cold Observable（新的观察者订阅后，会重新创建数据源并收到已产生的所有事件），有时需要将Cold Observable 转为 Hot Observable（多个观察者共享数据源，新观察者只收到自订阅后产生的数据）
        * 调用 .publish\(\) 可以将一个 Observable 转换成 ConnectableObservable，即可转换成 Hot Observable 只调用.publish\(\) ，观察者的 onNext\(\) 方法并不会调用，还需要调用 .connect\(\)方法，ConnectableObservable 才开始产生数据。refCount\(\)方法保证只要有观察者订阅，​ConnectableObservable 就回保持与数据源的连接
        * 调用 .share\(\) 方法也可以将一个 Cold Observable 转换成 Hot Observable，相当于调用 .publish\(\).refCount\(\)
      * Subjects 既可以当作数据源，又可以当做观察者。通过它也可以实现共享数据源的多点传播。
        * PublishSubject：观察者只能收到订阅后产生的数据
        * AsyncSubject：观察者只能收到 Observable 完成后的最后一个数据
        * BehaviorSubject：观察者只能收到最近产生的一个数据以及其后的所有数据
        * ReplaySubject：观察者能收到 Observable 产生过的所有旧数据以及之后产生的新数据
  * View 事件
    * 使用 RxBinding 可以将 View 的事件组织成 Observable
  * Disposable 和 Activity/Fragment 的生命周期 用于取消订阅，如果没有在合适的时机取消订阅，可能会产生内存泄漏
    * 不需要时就可以取消订阅，比如在 Activity 的 onDestroy\(\) onPause\(\)方法中
    * 使用 RxLiftCycle 库，可以在收到 Activity 或 Fragment 指定生命周期回调时自动取消订阅
    * 使用 Google 的 LifeCycle 库也可以

## **Backpressure\(背压\)**

> 解决数据产生速度比消费速度快的问题。如果生产者速度过快，则减慢其速度。

* Flowable 支持背压的 Observable
  * 支持的背压策略
    * BackpressureStrategy.ERROR:当下游观察者跟不上数据产生 速度时，抛出一个 MissingBackpressureException
    * **BackpressureStrategy.BUFFER（默认策略）:在下游观察者处理前，缓存已经产生但是未处理的数据（默认缓存数量为 128）**
    * BackpressureStrategy.DROP:丢弃最近产生的数据
    * BackpressureStrategy.LATEST:只保留最新产生的数据
    * BackpressureStrategy.MISSING:无背压，等同于直接使用 Observable
  * 什么时候用 Observable
    * 数据量小，不太可能出现 OOM
    * 处理 GUI 事件，不会很频繁
  * 什么时候用 Flowable 需要控制数据获取量
    * 数据量很大，可以控制产生的数据量
    * 文件读写：控制读取量
    * 数据库
    * 网络流
* Subscriber 比 Observer 多一个 onSubscribe\(Subcribtion s\)方法，Subcribtion 有一个 request\(long\)方法可以用来向数据源请求数据
* 限流
  * throttleFirst\(long,timeunit\) 、throttleFirst\(long,timeunit\):获取一段时间内的第一个或者最后一个事件
  * sample\(long,timeunit\):获取一段时间内最后一个事件（采样）
* 缓存
  * buffer:将一些数据缓存到 list
    * .buffer\(int count\): 缓存一定数量的数据
    * .buffer\(ObservableSource boundary\)：以 boundary 产生的数据为边界缓存数据
    * .buffer\(long timeSpan,TimeUnit unit\):缓存一定时间内的数据
  * window:将一些数据缓存成 Observable 便于链式操作甚至并发（类似于 flatMap）

## **Error Handling**

* 产生一个异常
  * observer.onError\(\)
  * Obserable.error\(\)
* Observer 的 onError\(\) 回调 这是一个终止事件，一旦产生错误，Observable 将不会产生新数据
* 严重的异常（如 OOM、VMError）不会传递给观察者，而是在异常产生线程直接抛出
* 异常处理操作
  * Observable.onErrorReturnItem\(T item\):发生异常时不调用 onError，而是返回该方法提供的 item
  * Observable.onErrorReturn\(Function valueSupplier\)：发生异常时，返回 valueSupplier 提供的 item 用于根据不同错误返回对应的 item
  * Observable.onErrorResumeNext\(ObservableSource next\)：发生异常时，由另一个 Observable 继续提供数据
* 重试操作 重试条件不满足后，继续调用Observer 的 onError
  * Observable.retry\(\):发生异常时重新订阅，让 Observable 继续完成数据产生 可以设置重试次数以及重试条件
  * Observable.retryWhen\(\) 可以设置每次重试的时机。例如指数延迟，下一次重试的等待时间是本次的2倍
* 处理未传递异常 异常发生时，没有提供您 onError 实现或者订阅关系已取消
  * 未处理异常会调用 RxJavaPlugin.onError\(\)默认打出堆栈信息
  * 可以通过 RxJavaPlugins.setErrorHandler\(\) 设置未传递异常处理函数，这样就不会调用RxJavaPlugin.onError\(\)（默认会 crash） 这种方式可以避免 UI 线程的崩溃，便于记录日志等

