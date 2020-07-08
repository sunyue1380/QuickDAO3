# 特殊查询

当您需要定制化返回结果时,请调用addColumn方法.以下是addColumn方法的几种使用场景.

## 返回单列

```java
List<Long> ids = dao.query(User.class)
                .addColumn("id")
                .execute()
                .getSingleColumnList(Long.class);
```

## 返回部分属性

```java
List<User> userList = dao.query(User.class)
                .addColumn("username","password")
                .execute()
                .getList();
```

## 分组聚合查询

```java
JSONArray array = dao.query(User.class)
                .addColumn("COUNT(ID)")
                .addColumn("max(id) as `M(ID)`")
                .groupBy("id")
                .having("count(id) = 1",null)
                .orderByDesc("max(id)")
                .execute()
                .getArray();
```


# Or查询

您可以添加or查询条件

```java
Condition condition = dao.query(Person.class)
                    .distinct()
                    .addQuery("firstName","a");
            condition.or()
                    .addQuery("lastName","a")
                    .addQuery("address","b")
                    .addQuery("city","c");
            Response response = condition.execute();
```

# union查询

您可以union多个表,但是您需要保证union关联的表的返回的字段保持一致.
您可以指定使用union或者union all方法进行连接

```java
Response response = dao.query(Person.class)
                    .union(dao.query(Person.class)
                            .addQuery("lastName","Gates")
                            .addColumns("id","lastName","firstName"))
                    .union(dao.query(Person.class)
                            .addQuery("firstName","Bill")
                            .addColumns("id","lastName","firstName"),UnionType.UnionAll)
                    .addQuery("address","Xuanwumen 11")
                    .addColumns(new String[]{"id","lastName","firstName"})
                    .orderByDesc("id")
                    .page(1,10)
                    .execute();
```