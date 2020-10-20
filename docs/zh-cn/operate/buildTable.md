# 建表删表

QuickDAO提供了手动建表删表功能,该功能对于编写测试用例时非常有用.

```java
//手动建表
dao.create(Person.class);
//手动删表
dao.drop(Person.class);
//重建表
dao.rebuild(Person.class);
```