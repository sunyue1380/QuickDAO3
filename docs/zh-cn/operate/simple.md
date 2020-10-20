# 简单更新操作

```java
//插入单个Person实例
Person person = new Person();
dao.insert(person);
//插入Person数组
Person[] persons = new Person[0];
dao.insert(persons);

/*
更新Person实例,若存在唯一性约束,则根据唯一性约束更新
否则若存在id,则根据id更新,
若都不存在,则忽略更新操作
*/

//更新单个用户实例
dao.update(person);
//更新用户数组
dao.update(persons);

/**
保存Person实例,
判断Person实例是否已经存在于数据库中(根据唯一性约束和id判断)
若存在则执行更新操作
若不存在则执行插入操作
*/

//保存Person实例
dao.save(person);
//保存Perrson数组实例
dao.save(persons);
```