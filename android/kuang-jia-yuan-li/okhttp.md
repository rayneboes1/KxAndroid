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



执行请求的方法  RealCall\#getResponseWithInterceptorChain\(\),这个方法中会将用户自定义的拦截器加上已经按职责分离的所有必须拦截器构造一个RealInterceptorChain，然后调用它的proceed 方法处理请求。

```text
internal fun getResponseWithInterceptorChain(): Response {
    // Build a full stack of interceptors.
    val interceptors = mutableListOf<Interceptor>()
    //添加用户自定义的拦截器
    interceptors += client.interceptors
    interceptors += RetryAndFollowUpInterceptor(client)
    interceptors += BridgeInterceptor(client.cookieJar)
    interceptors += CacheInterceptor(client.cache)
    interceptors += ConnectInterceptor
    if (!forWebSocket) {
      interceptors += client.networkInterceptors
    }
    interceptors += CallServerInterceptor(forWebSocket)

    val chain = RealInterceptorChain(
        call = this,
        interceptors = interceptors,
        index = 0,
        exchange = null,
        request = originalRequest,
        connectTimeoutMillis = client.connectTimeoutMillis,
        readTimeoutMillis = client.readTimeoutMillis,
        writeTimeoutMillis = client.writeTimeoutMillis
    )

    var calledNoMoreExchanges = false
    try {
      val response = chain.proceed(originalRequest)
      if (isCanceled()) {
        response.closeQuietly()
        throw IOException("Canceled")
      }
      return response
    } catch (e: IOException) {
      calledNoMoreExchanges = true
      throw noMoreExchanges(e) as Throwable
    } finally {
      if (!calledNoMoreExchanges) {
        noMoreExchanges(null)
      }
    }
  }
```

请求处理使用了连接器链完成-责任链模式。



{% embed url="https://请求完成后通过dispatcher.finish方法，将执行完的call移除队列，并在符合条件的情况下执行等待队列中的请求并添加到运行中队列。" %}

## 缓存

CacheInterceptor

