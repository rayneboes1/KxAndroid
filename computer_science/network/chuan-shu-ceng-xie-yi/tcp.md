---
description: Transmission Control Protocol
---

# TCP

## TCP

面向连接、提供可靠的数据传输。

## 报文结构

![TCP &#x62A5;&#x6587;&#x7ED3;&#x6784;](../../../.gitbook/assets/image%20%2814%29.png)

## 建立连接（三次握手）

* 客户端发送SYN报文段，SYN=1,seq=client\_start\_seq（其实序列号），进入 `SYN_SENT` 状态
* 服务端接收报文段，发送SYN\_ACK报文段，SYN=1，ACK=client\_start\_seq+1,seq=server\_start\_seq,进入 `SYN_REVD` 状态
* 客户端接收SYN\_ACK报文，发送一个报文段，SYN=0,ACK=sever\_start\_seq+1,可能会捎带数据，进入`Established`状态
* 服务端收到报文后，进入`Establish`状态

## 断开连接（四次挥手）

* 客户端发送FIN报文段，FIN=1，进入FIN\_WAIT\_1状态
* 服务端发送ACK，进入 CLOSE\_WAIT状态；客户端接收ACK 进入 FIN\_WAIT\_2 状态
* 服务端发送FIN 报文段，进入 LAST\_ACK 状态；客户端接收FIN报文段，并发送ACK，进入TIMED\_WAIT ，一段时间后关闭连接
* 服务端接收ACK后，关闭连接

## 流量控制（Flow Control）

使发送方发送速率不超过接收方接收速率。

通过让发送方维护一个接收窗口的变量，进行流量控制。

### 机制

假设 RcvBuffer 是接收方的接收缓存，应用缓存从接收缓存中读取数据；`lastByteRead` 是应用程序从缓存中读取最后一个字节的编号，`lastByteRcvd` 是放入接收缓存的最后一个接收到的字节编号。为了避免接收缓存溢出，必须保证：

`lastByteRcvd-lastByteRead<=RcvBuffer`

用 `RcvWindow` 表示接收窗口，则

`RevWindow=RcvBuffer-[lastByteRcvd-lastByteRead]`

即接收方最多还能接收 `RcvWindow` 个字节。

接收方将 `RcvWindow` 放入报文的接收窗口字段中，这样发送方就可以根据这个值来调整发送速率。发送方维护两个变量`lastByteSent`和`lastByteAcked`,而`lastByteSent-lastByteAcked`就是发送方已经发送但是未被接收方确认的数据量。通过保证：

`lastByteSent-lastByteAcked<=RcvWindow`

就能保证发送方的发送速率不超过接收方的接收速率。

### 小问题

当 RcvWindow=0,且接收方没有新的报文发送时，接收方无法得到RcvWindow的后续更新，此时即便接收方接收缓存已经可以继续接收数据，但它无法告诉发送方。因此TCP规定，当发送方接收到RcvWindow=0时，要继续发送只有一个字节数据的报文段，以此获取最新的 `RcvWindow` 值。

## 拥塞控制（Congestion Control）

控制流向整个整个网络中的数据量，避免拥塞引起丢包。

#### 如何控制

遏制发送方，发送方维护拥塞窗口变量 CongWin,保证 ：

```text
lastByteSent-lastByteAcked<=min(CongWin,revWindow)
```

通过限制未被确认的数据量，间接的控制了发送方发送的数据量。

#### 如何检测拥塞

出现超时或者收到接收方的三次冗余ACK（发生了丢包）

#### 避免拥塞策略

* 慢启动：CongWin 初始值为MSS，每过一个RTT\(往返时延\)CongWin翻倍，直到遇见丢包事或超时事件
* 加性增，乘性减:发生丢包后CongWin减半，以后每收到一个ACk，增加一个MSS\(最大报文段长，由链路层帧长度确定\)
* 对超时事件作出响应：超时后直接进入慢启动；三次冗余 CongWin 减半

## 相关问题

### TCP 和 UDP 的区别？

TCP 提供可靠传输，面向连接；UDP 不可靠，不建立连接

### 为什么要三次握手？

如果一次或者两次握手就算有效，那么可以轻易制造SYN洪泛导致的DDOS攻击；另外如果没有最后一次挥手，在网络拥堵时，无法保证客户端在服务端发出ACK后还有效，容易导致资源浪费。 再多次的握手没有意义。

### 为什么要四次挥手？

关闭连接需要确保双方都没有数据要传输了。前两次挥手确保服务端知道了客户端关闭的意愿，但此时服务端可能还有数据要传输，所以需要再挥一次，但如果只挥一次，没法保证客户端知道了服务端关闭意愿，因此还需要客户端发一次ACK。

### 为什么四次挥手后客户端还要进入TIMED\_WAIT？

如有必要则对 ACK 报文进行重传，避免报文段丢失。

### 拥塞控制和流量控制都是什么，两者的区别？

* 流量控制是为了平衡发送方的发送速率和接收方的读取速率，防止接收方缓存溢出
* 拥塞控制是为了避免整个网络链路过度拥堵，丢包率增加。

### 拥塞控制为什么对超时和三次冗余操作不同

三次冗余ACK代表虽然有丢包，但是网络状态还可以交付报文，而超时则意味着网络拥堵十分严重。

