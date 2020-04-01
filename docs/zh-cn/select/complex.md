# 复杂查询

QuickDAO提供了丰富的单表查询操作.

## Condition对象

调用``dao.query(User.class);``就得到了User类的Condition对象,Condition接口定义了大量添加条件查询的方法.

```java
Condition condition = dao.query(User.class);
condition.distinct()
    .addNullQuery("lastName")
    .addNotNullQuery("lastName")
    .addEmptyQuery("lastName")
    .addNotEmptyQuery("lastName")
    .addInQuery("lastName",new String[]{"1","2"})
    .addNotInQuery("lastName",new String[]{"3","4"})
    .addBetweenQuery("id",1,2)
    .addLikeQuery("lastName","a")
    .addQuery("lastName","=","a")
```

这些方法名见名知意,同时也有详细的JavaDoc文档.所有的查询条件接口以``add``开头,您可以很方便的分辨出哪些是查询方法接口.

## Response 对象

调用Condition实例的execute()方法就得到Response实例对象.Response接口定义获取返回结果的方法.不同的返回结果对应着不同的查询条件.

```java
Condition condition = dao.query(User.class);
Response response = condition.execute();
List<User> userList = response.getList();
```
