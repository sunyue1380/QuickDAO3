# 关联外键查询

QuickDAO提供了强大的外键关联查询,核心方法joinTable方法,且可以多次关联.

## 主表,子表,父表

为了方便叙述,我们需要先明确几个概念.

主表: query方法的参数即为主表
子表: joinTable方法的第一个参数为子表
父表: 子表再次关联时,原子表变为父表,新子表为子表.子表和父表是相对概念.

```java
dao.query(User.class)
     .joinTable(Address.class,"addressId","id");
```
在以上代码中,User是``主表``,Address是``子表``

```java
dao.query(User.class)
     .joinTable(Address.class,"addressId","id");
     .joinTable(City.class,"cityId","id")
```
在以上代码中,User是``主表``,Address是``子表``
同时Address和City的关系上,Address是父表,City是子表.

## 关联查询
```java
List<User> userList = dao.query(User.class)
     .joinTable(Address.class,"addressId","id")
     .addQuery("id",1)
     .joinTable(City.class,"cityId","id")
     .done()
     .execute()
     .getList();
```

* 当前查询是针对User表的查询,同时关联Address表,关联条件为User.addressId=Address.id
* 在Address表上添加查询条件,id = 1
* 同时Address又关联City表,关联条件为Address.cityId=City.id
* done方法返回到``主表``,doneSubCondition返回到``父表``

## 关联查询结果

若要返回关联查询实体,则在实体类中定义该实体类即可.例如以下实体类
```java
public class Address{
   private long id;
}

public class User{
   private long id;
   private Address address;
}
```

当想要在关联查询时填充和User类中的address属性,需要调用``compositField()``方法

```java
List<User> userList = dao.query(User.class)
     .joinTable(Address.class,"addressId","id")
     .done()
     .compositField()
     .execute()
     .getList();
```

> 默认情况下QuickDAO会根据实体类型自动寻找实体类成员变量中唯一匹配的成员变量.

若实体类中有多个需关联对象,则需要用户手动指定要关联的成员变量名.

```java
public class UserFollow {
    private long id;
    private long userId;
    private long followerId;
    private User user;
    private User followUser;
```

```java
List<UserSetting> userList = dao.query(UserFollow.class)
        //关联到User表的id字段,返回实体类信息放入变量名为user的实体类中
        .joinTable(User.class,"userId","id","user")
        .done()
        //关联到User表的id字段,返回实体类信息放入变量名为followUser的实体类中
        .joinTable(User.class,"userId","id","followUser")
        .done()
        .compositField()
        .execute()
        .getList();
```
