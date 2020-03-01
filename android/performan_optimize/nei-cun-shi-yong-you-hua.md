# 内存使用优化



## 内存优化

* 频繁使用字符串拼接用StringBuilder或者StringBuffer；ArrayMap、SparseArray替换HashMap；避免内存泄漏。
* 集合类泄漏\(集合一直引用着被添加进来的元素对象\)
* 单例/静态变量造成的内存泄漏\(生命周期长的持有了生命周期短的引用\)
* 匿名内部类/非静态内部类
* 资源未关闭造成的内存泄漏
* 检测内存泄漏的几个工具: LeakCanary，TraceView，Systrace，Android Lint和Memory Monitor+mat

