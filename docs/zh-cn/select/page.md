# 分页与排序

QuickDAO提供了非常简单的分页排序接口,由于只有查询操作需要分页排序,故这些方法放在了Condition接口中.

```java
Condition condition = dao.query(User.class)
       .page(1,10)
       .limit(0,10)
       .orderBy("id")
       .orderByDesc("username");
Response response = condition.execute();
List<User> userList = response.getList();
```

* page(int pageNum,int pageSize);

分页,第几页和每页个数

* limit(long offset, long limit);

分页,偏移量和返回个数

* orderBy(String field);

根据该字段升序排列

* orderByDesc(String field);

根据该字段降序排列
