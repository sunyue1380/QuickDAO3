# 事务操作

QuickDAO封装了事务操作.事务操作只适用于更新操作,查询操作无事务操作.

* 设置隔离级别

```java
Transaction transaction = dao.startTransaction();
transaction.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
```

* 开启事务

```java
Transaction transaction = dao.startTransaction();
transaction.insert(user);
transaction.commit();
transaction.endTransaction();
```

> Transaction接口上的方法调用后,必须调用commit()方法才会提交事务,默认不会提交事务

* 回滚事务

```java
transaction.rollback();
```
