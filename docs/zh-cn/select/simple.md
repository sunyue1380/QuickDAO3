# 简单查询

QuickDAO提供了一些便捷方法.当您只是根据单个条件查询或者只需要返回单条结果时,这些方法是非常有用的.

## Person实体类
```java
public class Person {
    @Id(strategy = IdStrategy.AutoIncrement)
    private long id;
    private String username;
    private String password;
}
```

## 简单查询实例

```java
//根据id查询
//对应SQL: select id,username,password from person where id = 1
User user1 = dao.fetch(Person.class,1);

//根据单个属性查询,返回列表的第一条数据
//对应SQL: select id,username,password from person where username = 'quickdao'
User user2 = dao.fetch(User.class,"username","quickdao");

//根据单个属性查询,返回列表
//对应SQL: select id,username,password from person where username = 'quickdao'
List<User> userList = dao.fetchList(User.class,"username","quickdao");
```

> fetch方法默认只会返回列表的第一条记录,若返回列表存在多条记录,则会直接返回第一条记录