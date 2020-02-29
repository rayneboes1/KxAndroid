---
description: 'Domain Name System,域名系统'
---

# DNS

## 概述

提供域名到IP的查询服务，应用层协议，**运行在 UDP 上，**使用`53` 端口。

DNS 服务器组织成层级结构：

* 根DNS服务器
* 顶级域服务器\(.com .cn\)
* 权威服务器（qq.com）

## 查询

![DNS  &#x67E5;&#x8BE2;&#x8FC7;&#x7A0B;](../../../.gitbook/assets/image%20%2834%29.png)

存在递归和迭代过程，上图中客户端到本地DNS服务器是递归 ，其他是迭代。

可以利用缓存提高效率。（设置缓存有效期）

