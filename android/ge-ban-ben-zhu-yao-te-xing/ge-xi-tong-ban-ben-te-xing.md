# 各系统版本特性

## Android 10-API Level 29

* 支持可折叠、5G等新型设备
* 通知智能回复
* 系统深色主题
* 手势导航（应用全屏显示内容）
* 隐私增强\(应用是否可以在后台获取位置权限、应用无法访问不可重置的设备标识符\)
* 阻止应用后台启动
* 默认启用TLS 1.3
* 限制外部存储，应用文件可以存储在专用目录中，访问共享文件需要使用MediaStore
* 后台定位单独权限需兼容
* 设备唯一标示符需兼容
* 后台打开Activity 需兼容
* 非 SDK 接口限制需兼容

## Android 9.0-API Level 28

* 利用 Wi-Fi RTT 进行室内定位
* 刘海屏 API 支持
* 多摄像头支持和摄像头更新
* 不允许调用hide api
* 限制明文流量的网络请求 http

## Android 8-\(8.0-&gt;API Level 26,8.1-&gt;API Level 27\)

* 通知
* 画中画
* 自动填充
* 后台限制
* 自适应桌面图标-&gt;适配
* 隐式广播限制
* 开启后台Service限制
* clear text not permitted

## Android 7-API \(7.0-&gt;API Level 24,7.1.1-&gt;Level 25\)

* 多窗口模式
* 支持Java 8语言平台
* 需要使用FileProvider访问照片
* 安装apk需要兼容

## Android 6- API  Level 23

* 应用权限管理
* 官方指纹支持
* Doze电量管理
* 运行时权限机制-&gt;需要动态申请权限

## Android 5-\(5.0-&gt;API Level 21,5.1-&gt;API Level 22\)

* Material Design
* 运行时由Dalvik 变为  ART虚拟机

### ART 相比  Dalvik 做了哪些优化？

> [官方文档](https://source.android.google.cn/devices/tech/dalvik/index.html)

#### 预先（AOT）编译

引入预先编译，提高性能，拥有更严格的安装验证

#### **垃圾回收优化**

* 只有一次（而非两次）GC 暂停
* 在 GC 保持暂停状态期间并行处理
* 在清理最近分配的短时对象这种特殊情况中，回收器的总 GC 时间更短
* 优化了垃圾回收的工效，能够更加及时地进行并行垃圾回收，这使得 [`GC_FOR_ALLOC`](http://developer.android.google.cn/tools/debugging/debugging-memory.html#LogMessages) 事件在典型用例中极为罕见
* 压缩 GC 以减少后台内存使用和碎片

#### **针对开发和调试优化**

* 增加采样分析 ，方便分析应用运行情况
* 支持更多调试功能 
* 优化异常和崩溃的详情信息

