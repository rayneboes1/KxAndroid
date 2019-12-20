# 数据存储

Android中提供哪些数据持久存储的方法？

* SharedPreferences: 小东西,最终是xml文件中,key-value的形式存储的.
* 文件
* 数据库
* ContentProvider
* 网络

Q：Java中的I/O流读写怎么做？

Q：SharePreferences适用情形？使用中需要注意什么？

尽量使用小文件，减少内存压力；提交时使用apply

* Q：了解SQLite中的事务处理吗？是如何做的？
* Q：使用SQLite做批量操作有什么好的方法吗？

Q：如果现在要删除SQLite中表的一个字段如何做？

重新创建表，复制数据

* Q：使用SQLite时会有哪些优化操作?

