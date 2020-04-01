# 建表删表

QuickDAO提供了手动建表删表功能,该功能对于编写测试用例时非常有用.

* 手动建表

```java
dao.create(User.class);
```

* 手动删表

```java
dao.drop(User.class);
```

* 重建表

```java
dao.rebuild(User.class);
```
