# 特殊查询

QuickDAO提供了分组查询,部分字段查询和union查询等.

## 单属性查询
```java
List<Long> ids = dao.query(User.class)
    .execute()
    .getValueList(Long.class,"id");
```

## 聚合查询
```java
JSONArray array = dao.query(Person.class)
                    .addAggerate("count","id")
                    .addAggerate("max","id","m(id)")
                    .orderByDesc("m(id)")
                    .execute()
                    .getAggerateList();
```

* 添加查询条件 ``addAggerate``
* 获取聚合查询结果 ``getAggerateList``

## 部分字段查询
```java
List<Person> response = dao.query(Person.class)
                    .addQuery("lastName","Gates")
                    .excludeColumn("city")
                    .execute()
                    .getPartList();
```

* 添加查询条件 ``addColumn``或者``excludeColumn``
* 获取聚合查询结果 ``getPartList``

> addColumn参数只能为表中的字段,不能在其上施加函数等操作.若有自定义字段需要,需要使用特殊字段查询

## 特殊字段查询
```java
JSONArray array = dao.query(Person.class)
                    .addQuery("lastName","Gates")
                    .addSpecialColumn("max("+ Condition.mainTableAlias +".id) as m")
                    .execute()
                    .getSpecialList();
```

* 添加查询条件 ``addSpecialColumn``
* 获取聚合查询结果 ``getSpecialList``

## Or查询
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

``condition.or()``返回一个Condition实例,在此对象上施加的查询条件都将作为一个整体的or查询添加到查询语句中.

## union查询
```java
Response response = dao.query(Person.class)
                    .union(dao.query(Person.class)
                            .addQuery("lastName","Gates")
                            .addColumns(new String[]{"id","lastName","firstName"}))
                    .union(dao.query(Person.class)
                            .addQuery("firstName","Bill")
                            .addColumns(new String[]{"id","lastName","firstName"}),UnionType.UnionAll)
                    .addQuery("address","Xuanwumen 11")
                    .addColumns(new String[]{"id","lastName","firstName"})
                    .orderByDesc("id")
                    .page(1,10)
                    .execute();
```

``condition.union()``方法需要传递一个Condition实例,您需要保证该参数为相同类的Condition实例,并且返回字段个数也必须相同.
