# OkHttp

[彻底理解OkHttp - OkHttp 源码解析及OkHttp的设计思想](https://juejin.im/post/5c1b23b9e51d4529096aaaee) 

## OkHttpClient 创建

可以通过建造者模式进行参数设定，会初始化调度器Dispatcher。

OkHttpClient 的创建、



## 请求创建

请求的创建等都使用了建造者模式。

## 异步请求

请求真正的执行时RealCall类，同步请求使用execute，异步请求使用enqueue

Realcal\#enqueue 调用了 client.dispatcher\#enqueue\(AsyncCall\)

入队时会有条件判断（并发请求数量、相同host并发请求数量、线程数等等），满足条件请求被放入执行队列中，通过通过线程池发送队列中的请求；否则加入到等待队列中。

请求处理使用了连接器链完成-责任链模式。

