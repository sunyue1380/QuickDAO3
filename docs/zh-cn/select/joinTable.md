# 关联外键查询

QuickDAO提供了强大的外键关联查询,核心方法为joinTable方法.

## 实体类信息

```java

/**Person类,表示人*/
public class Person{
    @Id(strategy = IdStrategy.AutoIncrement)
    private long id;
    //省略get/set方法
}

/**Order类,订单类*/
public class Order{
    @Id(strategy = IdStrategy.AutoIncrement)
    private long id;
    @ForeignKey(table = Person.class)
    private long personId;
    
    private Person person;
    //省略get/set方法
}

/**地址类,每个订单有一个地址信息*/
public class Address{
    @Id(strategy = IdStrategy.AutoIncrement)
    private long id;

    private String name;

    @ForeignKey(table = Order.class)
    private long orderId;
    
    private Order order;
    //省略get/set方法
}
```

## 主表,子表,父表

```java
//from person
dao.query(Person.class)
    //join order on person.id = order.person_id 
    .joinTable(Order.class,"id","person_id")
    //join address on order.id = address.order_id
    .joinTable(Address.class,"id","order_id")
    .doneSubCondition()
    .done();
```

joinTable方法用于关联表,方法定义如下:
```java
<E> SubCondition<E> joinTable(Class<E> clazz, String primaryField, String joinTableField);
```

参数含义如下:

* clazz: 要join的表
* primaryField: **主表**的关联字段
* joinTableField: **子表**的关联字段

其中主表,子表的定义如下:

* 主表: query方法的参数即为主表
* 子表: joinTable方法的第一个参数为子表

上述例子中,Person为主表,Order为子表.

此外,子表还可以再次关联表,此时原子表称为**父表**,父表和子表是相对关系

上述例子中,Order是Address的父表,Address是Order的子表

调用done方法返回**主表**,doneSubCondition方法返回**父表**

## 表别名

默认情况下,主表别名为t,依次关联的子表分别为t1,t2,t3......

## 关联查询

调用joinTable方法后一样可调用addQuery系列方法添加查询参数.此时的查询是针对join的表所添加的查询

```java
//from person
dao.query(Person.class)
    //join order on person.id = order.person_id 
    .joinTable(Order.class,"id","person_id")
    //order.person_id > 0
    .addQuery("person_id",">",0)
    //join address on order.id = address.order_id
    .joinTable(Address.class,"id","order_id")
    //address.name = 'quickdao'
    .addQuery("name","quickdao")
    .doneSubCondition()
    .done();
```

## 关联查询结果

若要返回关联查询实体,则必须手动调用compositField方法.

```java
//from person
dao.query(Person.class)
    //join order on person.id = order.person_id 
    .joinTable(Order.class,"id","person_id")
    //order.person_id > 0
    .addQuery("person_id",">",0)
    //join address on order.id = address.order_id
    .joinTable(Address.class,"id","order_id")
    //address.name = 'quickdao'
    .addQuery("name","quickdao")
    .doneSubCondition()
    .done()
    //调用以下方法后Order类的Person对象和Address类的Order对象会自动填充
    .compositField();
```

> 若未调用CompositField方法不会返回关联表属性信息.

> 默认情况下QuickDAO会根据实体类型自动寻找实体类成员变量中唯一匹配的成员变量.

# 多属性关联

若实体类中有多个需关联对象,则需要用户手动指定要关联的成员变量名.

```java
public class Person {
    private long id;
    private long motherId;
    private long fatherId;
    private Person mother;
    private Person father;
}
```

```java
List<Person> userList = dao.query(Person.class)
        //person1.mother_id = person2.id,关联属性到mother变量
        .joinTable(Person.class,"mother_id","id","mother")
        .done()
        //person1.father_id = person2.id,关联属性到father变量
        .joinTable(Person.class,"father_id","id","father")
        .done()
        .compositField()
        .execute()
        .getList();
```
# 设置表别名

> 此方法从v3.3开始提供 

* 设置主表别名

```java
dao.query(Person.class).tableAliasName("p");
```

* 设置子表别名

```java
dao.query(Person.class)
    .joinTable(Person.class,"motherId","id","mother")
    .tableAliasName("motherTable")
    .done()
    .joinTable(Person.class,"fatherId","id","father")
    .tableAliasName("fatherTable")
    .done()
```

## 关联Condition

> 此功能从v3.6开始提供

从v3.6版本开始,joinTable支持将Condition接口作为参数传入
```java
//select id,username,password,type from person as t join (select person_id,count(person_id) count from `order` group by person_id having count(person_id) > 0) t1 on t.id = t1.person_id where t.person_id = 1 order by t.id desc
Condition joinCondition = dao.query(Order.class)
                    .addColumn("person_id","count(person_id) count")
                    .groupBy("personId")
                    .having("count(person_id) > 0");
            Response response = dao.query(Person.class)
                    .joinTable(joinCondition,
                            "id","person_id")
                    .done()
                    .addQuery("person_id",1)
                    .orderByDesc("id")
                    .execute();
```

## cross join

> 此功能从v3.6开始提供

从v3.6版本开始,QuickDAO支持crossJoin方法

```java
//select id,username,password,type from person as t cross join order as t1 where t.id =t1.person_id and t1.order_no > 0
List<Person> personList = dao.query(Person.class)
                    .crossJoinTable(Order.class)
                    .addQuery("orderNo",">",0)
                    .done()
                    .addRawQuery("t.id = t1.person_id")
                    .execute()
                    .getList();
```
