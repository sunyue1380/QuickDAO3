# 批量操作

Condition接口提供了批量更新和根据查询条件删除的功能

* 批量更新

```java
int count = dao.query(Person.class)
     .addQuery("username","quickdao")
     .addUpdate("password","123456")
     .execute()
     .update();
```


* 批量删除

```java
int count = dao.query(Person.class)
     .addQuery("username","quickdao")
     .execute()
     .delete();
```

