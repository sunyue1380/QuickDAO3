# 简单查询

QuickDAO提供了一些便捷方法.当您只是根据单个条件查询或者只需要返回单条结果时,这些方法是非常有用的.

```java
//根据id查询
User user1 = dao.fetch(User.class,1);
//根据单个属性查询,返回列表的第一条数据
User user2 = dao.fetch(User.class,"username","quickdao");
//根据单个属性查询,返回列表
List<User> userList = dao.fetchList(User.class,"username","quickdao");
```