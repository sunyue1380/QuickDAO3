# 简单更新操作

```java
//插入单个User实例
User user = new User();
dao.insert(user);
//插入User数组
User[] users = new User[0];
dao.insert(users);

/*
更新user实例,若存在唯一性约束,则根据唯一性约束更新
否则若存在id,则根据id更新,
若都不存在,则忽略更新操作
*/

//更新单个用户实例
dao.update(user);
//更新用户数组
dao.update(users);

/**
保存user实例,
判断User实例是否已经存在于数据库中(根据唯一性约束和id判断)
若存在则执行更新操作
若不存在则执行插入操作
*/

//保存User实例
dao.save(user);
//保存User数组实例
dao.save(users);
```