# 复杂查询

QuickDAO提供了丰富的单表查询操作.

## Person实体类
```java
public class Person {
    @Id(strategy = IdStrategy.AutoIncrement)
    private long id;
    private String username;
    private String password;
    private int type;
}
```

## Condition对象

调用``dao.query(Person.class);``就得到了Person类的Condition对象,Condition接口定义了大量添加条件查询的方法.

```java
Condition condition = dao.query(Person.class)
    //添加distinct关键字
    .distinct()
    //username is null
    .addNullQuery("username")
    //username is not null
    .addNotNullQuery("username")
    //username is not null and username = ''
    .addEmptyQuery("username")
    //username is not null and username != ''
    .addNotEmptyQuery("username")
    //type in (1,2)
    .addInQuery("type","1","2")
    //type not in (3,4)
    .addNotInQuery("type","3","4")
    //type between 1 and 2
    .addBetweenQuery("type",1,2)
    //username like 'quickdao%'
    .addLikeQuery("username","quickdao%")
    //type >= 1
    .addQuery("type",">=","1")
```

这些方法名见名知义,同时也有详细的JavaDoc文档.所有的查询条件接口以``addXXXQuery``命名,您可以很方便的分辨出哪些是查询方法接口.

## or查询

您可以添加or查询条件

```java
//select distinct id,username,password,type from person where username like 'a%' or username like 'b%' 
Condition condition = dao.query(Person.class)
           .distinct()
            .addLikeQuery("username","a%");
condition.or().addQuery("username","b%")
Response response = condition.execute();
```

# union查询

您可以union多个表,但是您需要保证union关联的表的返回的字段个数保持一致.

您可以指定使用union或者union all类型进行连接,union方法默认采用union连接方式.

```java
Condition unionCondition1 = dao.query(Person.class)
    .addQuery("username","a")
    .addColumn("username","password");

Condition unionCondition2 = dao.query(Person.class)
    .addQuery("username","b")
    .addColumn("username","password");

Response response = dao.query(Person.class)
                    .union(unionCondition1)
                    .union(unionCondition2,UnionType.UnionAll)
                    .addColumn("username","password")
                    .execute();
```

## addRawQuery

> 此方法v3.6版本开始提供

addRawQuery方法将给定的参数直接拼接到SQL语句上,适用于查询条件无法用现有API生成的情况

```java
Condition condition = dao.query(Person.class)
     .addRawQuery("username = (select max(username) from person where age = t.age) ")
```

## Response 对象

调用Condition实例的execute()方法就得到Response实例对象.Response接口定义获取返回结果的方法.不同的返回结果对应着不同的查询条件.

```java
Condition condition = dao.query(User.class);
Response response = condition.execute();
List<User> userList = response.getList();
```

## clone方法

> 此功能从v3.6开始提供

从3.6版本开始,Condition实现了Cloneable接口.
