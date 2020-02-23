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

### proceed

```text
override fun proceed(request: Request): Response {
    check(index < interceptors.size)

    calls++

    if (exchange != null) {
      check(exchange.connection.supportsUrl(request.url)) {
        "network interceptor ${interceptors[index - 1]} must retain the same host and port"
      }
      check(calls == 1) {
        "network interceptor ${interceptors[index - 1]} must call proceed() exactly once"
      }
    }

    // Call the next interceptor in the chain.
    val next = copy(index = index + 1, request = request)
    val interceptor = interceptors[index]

    @Suppress("USELESS_ELVIS")
    val response = interceptor.intercept(next) ?: throw NullPointerException(
        "interceptor $interceptor returned null")

    if (exchange != null) {
      check(index + 1 >= interceptors.size || next.calls == 1) {
        "network interceptor $interceptor must call proceed() exactly once"
      }
    }

    check(response.body != null) { "interceptor $interceptor returned a response with no body" }

    return response
  }
```

intercept 方法参数为 RealInterceptorChain ，该方法中需要执行 chain.proceed 进行传递。

如果要在响应前执行操作，那么可以放在 chain.proceed 之前；如果要在收到响应后再处理，可以先调用 chain.proceed 获取响应，然后再操作。

真正发送请求的拦截器是CallServerInterceptor，这也是拦截器列表里最后一个拦截器。

请求完成后通过 dispatcher.finish方法，将执行完的call移除队列，并在符合条件的情况下执行等待队列中的请求并添加到运行中队列。

## 同步请求

```text
// RealCall#execute()
override fun execute(): Response {
    synchronized(this) {
      check(!executed) { "Already Executed" }
      executed = true
    }
    timeout.enter()
    callStart()
    try {
      client.dispatcher.executed(this)
      return getResponseWithInterceptorChain()
    } finally {
      client.dispatcher.finished(this)
    }
  }
```

dispatcher 只是将 call 添加到了运行中队列：

```text
@Synchronized internal fun executed(call: RealCall) {
    runningSyncCalls.add(call)
 }
```

## Dispatcher

Dispatcher 有三个队列

```text
//等待中的异步请求
private val readyAsyncCalls = ArrayDeque<AsyncCall>()

//处理中的异步请求
private val runningAsyncCalls = ArrayDeque<AsyncCall>()

//处理中的同步请求
private val runningSyncCalls = ArrayDeque<RealCall>()
```

### 线程池创建

```text
val executorService: ExecutorService
    get() {
      if (executorServiceOrNull == null) {
        executorServiceOrNull = ThreadPoolExecutor(0, Int.MAX_VALUE, 60, TimeUnit.SECONDS,
            SynchronousQueue(), threadFactory("$okHttpName Dispatcher", false))
      }
      return executorServiceOrNull!!
    }
```

默认情况下创建的线程池，核心线程数是0（没有常驻线程）、存活时间60秒。默认拒绝策略是抛异常。

## 请求发送



## 缓存处理

默认的缓存逻辑在 CacheInterceptor 中，使用DiskLruCache。

```text
override fun intercept(chain: Interceptor.Chain): Response {
    val cacheCandidate = cache?.get(chain.request())

    val now = System.currentTimeMillis()

    val strategy = CacheStrategy.Factory(now, chain.request(), cacheCandidate).compute()
    val networkRequest = strategy.networkRequest
    val cacheResponse = strategy.cacheResponse

    cache?.trackResponse(strategy)

    if (cacheCandidate != null && cacheResponse == null) {
      // The cache candidate wasn't applicable. Close it.
      cacheCandidate.body?.closeQuietly()
    }

    // If we're forbidden from using the network and the cache is insufficient, fail.
    if (networkRequest == null && cacheResponse == null) {
      return Response.Builder()
          .request(chain.request())
          .protocol(Protocol.HTTP_1_1)
          .code(HTTP_GATEWAY_TIMEOUT)
          .message("Unsatisfiable Request (only-if-cached)")
          .body(EMPTY_RESPONSE)
          .sentRequestAtMillis(-1L)
          .receivedResponseAtMillis(System.currentTimeMillis())
          .build()
    }

    // If we don't need the network, we're done.
    if (networkRequest == null) {
      return cacheResponse!!.newBuilder()
          .cacheResponse(stripBody(cacheResponse))
          .build()
    }

    var networkResponse: Response? = null
    try {
      networkResponse = chain.proceed(networkRequest)
    } finally {
      // If we're crashing on I/O or otherwise, don't leak the cache body.
      if (networkResponse == null && cacheCandidate != null) {
        cacheCandidate.body?.closeQuietly()
      }
    }

    // If we have a cache response too, then we're doing a conditional get.
    if (cacheResponse != null) {
      if (networkResponse?.code == HTTP_NOT_MODIFIED) {
        val response = cacheResponse.newBuilder()
            .headers(combine(cacheResponse.headers, networkResponse.headers))
            .sentRequestAtMillis(networkResponse.sentRequestAtMillis)
            .receivedResponseAtMillis(networkResponse.receivedResponseAtMillis)
            .cacheResponse(stripBody(cacheResponse))
            .networkResponse(stripBody(networkResponse))
            .build()

        networkResponse.body!!.close()

        // Update the cache after combining headers but before stripping the
        // Content-Encoding header (as performed by initContentStream()).
        cache!!.trackConditionalCacheHit()
        cache.update(cacheResponse, response)
        return response
      } else {
        cacheResponse.body?.closeQuietly()
      }
    }

    val response = networkResponse!!.newBuilder()
        .cacheResponse(stripBody(cacheResponse))
        .networkResponse(stripBody(networkResponse))
        .build()

    if (cache != null) {
      if (response.promisesBody() && CacheStrategy.isCacheable(response, networkRequest)) {
        // Offer this request to the cache.
        val cacheRequest = cache.put(response)
        return cacheWritingResponse(cacheRequest, response)
      }

      if (HttpMethod.invalidatesCache(networkRequest.method)) {
        try {
          cache.remove(networkRequest)
        } catch (_: IOException) {
          // The cache cannot be written.
        }
      }
    }

    return response
  }
```

主要逻辑：

1. 校验缓存是否可用
2. 网络不可用、缓存不可用，返回空响应
3. 网络不可用、缓存可用，返回缓存的响应
4. 网络可用、发送请求，如果304，返回缓存可用，
5. 根据最新的响应更新或添加缓存

