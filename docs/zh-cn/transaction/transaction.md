# 事务操作

QuickDAO封装了事务操作.事务操作只适用于更新操作,查询操作无事务操作.

```java
//开启事务
Transaction transaction = dao.startTransaction();
//设置事务隔离级别
transaction.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
//插入用户信息
transaction.insert(user);
//提交
transaction.commit();
//回滚
//transaction.rollback();
//结束事务
transaction.endTransaction();
```

> 使用事务进行插入,更新和删除数据后,请务必手动调用commit方法或者rollback方法. 默认情况下QuickDAO不会自动提交事务

# Condition事务操作

> 此功能从v3.6.6开始提供

从3.6.6版本开始,事务操作也支持query方法了.

```java
//Condition事务
Transaction transaction = dao.startTransaction();
transaction.query("person")
        .addInsert("password","123456")
        .addInsert("first_name","John")
        .addInsert("last_name","Adams")
        .addInsert("address","Oxford Street")
        .addInsert("city","London")
        .execute()
        .insert();
transaction.commit();
//回滚
//transaction.rollback();
//结束事务
transaction.endTransaction();
```