# 特殊查询

## addColumn方法

addColumn方法用于指定返回的列信息,即select {{column}} from table 中的column部分.
基于此,addColumn的用法非常多样,同时需要调用对应的Response接口里面定义的方法.

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

### 返回单属性

```java
//select id from person 返回第一行的id属性
String id = (String)dao.query(Person.class)
                .addColumn("id")
                .execute()
                .getSingleColumn(String.class);
```

### 返回单列

```java
//select id from person
List<Long> ids = dao.query(Person.class)
                .addColumn("id")
                .execute()
                .getSingleColumnList(Long.class);
```

## 返回部分属性

```java
//select username,password from person
List<Person> personList = dao.query(Person.class)
                .addColumn("username","password")
                .execute()
                .getList();
```

## 分组聚合查询

```java
//select COUNT(ID) as count,max(id) as \"M(ID)\" from person group by id having count(id) = 1 order by max(id)
JSONArray array = dao.query(Person.class)
        .addColumn("count(id) as count")
        .addColumn("max(id) as \"M(ID)\"")
        .groupBy("id")
        .having("count(id) = 1",null)
        .orderByDesc("max(id)")
        .execute()
        .getArray();
```