# 更新日志

## v3.6.1-3.6.6
* \[新增]事务操作支持query方法
* \[新增]批量插入时默认会返回自增id
* \[新增]虚拟查询新增批量插入方法
* \[优化]解决可能存在的数据库连接泄露问题
* \[优化]支持LocalDate和LocalDateTime类型

## v3.6
* \[新增]全部支持子查询功能
* \[新增]SQL执行失败时会打印原始SQL语句

## v3.5
* \[新增]Condition接口添加addInsert方法以及Response接口添加insert方法,支持动态数据
* \[新增]添加query(String tableName)和joinTable(String tableName....)方法,支持无实体类查询和关联查询操作

## v3.4
* \[新增]joinTable支持连接子查询
* \[新增]支持自定义Id生成器
* \[删除]删除Response接口的返回聚合,特殊查询等接口,全部通过getArray接口返回

## v3.3
* \[新增]joinTable后可以手动指定表别名
* \[新增]添加reverse方法,根据数据库
* \[更新]打印SQL日志时显示影响行数和耗时
* \[更新]解决实例无实体id属性时报错问题