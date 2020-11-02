# 关联子查询

> 此功能从v3.6版本开始提供

QuickDAO从3.6版本开始支持子查询功能,您可以将Condition接口对象作为子查询参数传入.

## 实体类信息

```java
public class Product {
    @Id(strategy = IdStrategy.IdGenerator)
    private long id;

    @Comment("商品名称")
    private String name;

    @Comment("商品类别")
    private String type;

    @Comment("商品价格")
    private int price;

    @TableField(createdAt = true)
    private Date publishTime;
}
```

## where子查询

```java
//select count(1) from product where price < (select avg(price) from product)
long count = dao.query(Product.class)
                .addSubQuery("price","<",dao.query(Product.class).addColumn("avg(price)"))
                .execute()
                .count();
```

## having子查询

```java
//select count(type) count from product group by type having count(type) > (select 1 from dual)
Condition havingCondition = dao.query("dual")
                .addColumn("1");
long count = (long) dao.query(Product.class)
        .groupBy("type")
        .having("count(type)",">",havingCondition)
        .addColumn("count(type) count")
        .execute()
        .getSingleColumn(Long.class);
```

## from子查询

```java
//select type avgPrice from (select type, avg(price) avgPrice from product group by type) where avgPrice >= 2000
Condition<Product> fromCondition = dao.query(Product.class)
                .groupBy("type")
                .addColumn("type")
                .addColumn("avg(price) avgPrice");
JSONArray array = dao.query(fromCondition)
               .addQuery("avgPrice",">=",2000)
               .addColumn("type","avgPrice")
               .execute()
               .getArray();
```

## select子查询

```java
//select (select name from dual) nameAlias from product 
Condition selectCondition = dao.query("dual")
                .addColumn("name");
List<String> productNameList = dao.query(Product.class)
                .addColumn(selectCondition,"nameAlias")
                .execute()
                .getSingleColumnList(String.class);
```

## exist子查询

```java
//select name from product where exists (select id from product where price >= 5000)
List<String> productNameList = dao.query(Product.class)
                .addExistSubQuery(
                        dao.query(Product.class)
                                .addQuery("price",">=",5000)
                                .addColumn("id")
                )
                .addColumn("name")
                .execute()
                .getSingleColumnList(String.class);
```