# 分页与排序

QuickDAO提供了非常简单的分页排序接口,由于只有查询操作需要分页排序,故这些方法放在了Condition接口中.

```java
Condition condition = dao.query(User.class)
       //分页,第几页和每页个数
       .page(1,10)
       //分页,偏移量和返回个数
       .limit(0,10)
       //根据该字段升序排列
       .orderBy("id")
       ////根据该字段升序排列
       .orderByDesc("username");
Response response = condition.execute();
List<User> userList = response.getList();
```

> 请注意,当您使用tableAliasName方法指定了表别名时,在调用排序方法时请加上该别名