# HTTP

## 相关题目

### 了解哪些响应状态码？

200 400 403 404 500 304\(not modify\)

### get 和 post 的区别？

get 会把数据直接放到url中，一般用于获取数据；

post 把数据放到请求体中，一般用于上传数据，避免直接通过url获取请求参数。

### HTTP和TCP的区别?

### HTTP 和 Socket 的区别?

socket 是长连接，http 会主动关闭连接。

## Http1.0、Http1.1、Http2.0的区别？

## HTTP 2.0

### 使用二进制帧

将原有的文本格式改为二进制帧的形式，每个帧有一个stream id标识，每个请求都对应一个stream id。

![](../../../.gitbook/assets/image%20%287%29.png)

### 连接复用

旧版协议中针对每个域名有连接数限制。在一个连接中，如果有很多请求，在前面的请求响应未返回之前，后面的请求就只能等待。而且新建连接三次握手也会增加时延。

HTTP 2.0 由于将数据组织成帧，所以可以同时发送数据帧，收到响应后根据stream id 区分请求，大大减少了时延。同时也能减少服务端链接压力，降低网络拥塞。

另外，还可以给 stream 设置优先级，可以使重要的请求优先被发送。 也可以设置请求之间的依赖。

![](../../../.gitbook/assets/image%20%2812%29.png)

### Header 压缩

将 header 信息压缩，减少数据量，使用了 [HPACK](https://http2.github.io/http2-spec/compression.html) 算法。

### ServerPush

在HTTP/2中，服务器可以对客户端的一个请求发送多个响应，可以先把内容推送给客户端。

## 相关链接

[HTTP/2 相比 1.0 有哪些重大改进？ - victor yu的回答 - 知乎](https://www.zhihu.com/question/34074946/answer/108588042)

 

