---
description: adb 常用指令集合
---

# ADB

## 帮助

`adb help`

## 设备相关

### 查看链接的设备

`adb devices [-l]`

查看所有链接的设备，-l 参数会显示详细信息

```text
zhaowenhai@zhaowenhaideMacBook-Pro ~ % adb devices
List of devices attached
05157df5089e4219	device

zhaowenhai@zhaowenhaideMacBook-Pro ~ % adb devices -l
List of devices attached
05157df5089e4219       device usb:336723968X product:zerofltezh model:SM_G9200 device:zerofltechn transport_id:7
```

## 应用

### 安装应用

```text
adb install [-lrtsdg] [--instant] PACKAGE
// 实例
adb install apk/debug.apk
```

### 卸载应用

```text
adb uninstall [-k] PACKAGE
remove this app package from the device
'-k': keep the data and cache directories
```

