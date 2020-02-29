# WorkManager

## 简介

* 最高向后兼容到 API 14
  * 在运行 API 23 及以上级别的设备上使用 JobScheduler
  * 在运行 API 14-22 的设备上结合使用 BroadcastReceiver 和 AlarmManager
* 添加网络可用性或充电状态等工作约束
* 调度一次性或周期性异步任务
* 监控和管理计划任务
* 将任务链接起来
* 确保任务执行，即使应用或设备重启也同样执行任务
* 遵循低电耗模式等省电功能

WorkManager 旨在用于**可延迟**运行（即不需要立即运行）并且在应用退出或设备重启时必须能够可靠运行的任务。例如：

* 向后端服务发送日志或分析数据
* 定期将应用数据与服务器同步

WorkManager 不适用于应用进程结束时能够安全终止的运行中后台工作，也不适用于需要立即执行的任务。

## 使用

通过 `Worker` 指定具体工作，通过 WorkRequest 创建工作请求（单次或循环等），然后提交到WorkManager 队列中等待执行。还可以给任务设定前置条件（电量、网络状态等）。

## 相关链接

[使用 WorkManager 调度任务](https://developer.android.com/topic/libraries/architecture/workmanager)

