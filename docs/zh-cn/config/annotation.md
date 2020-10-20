# 实体注解

QuickDAO提供了实体类注解用于用户自定义字段属性.

## @Id

标识Id字段,作用于类字段上

```java
public class User{
    @Id
    private long uid;
}
```

> 此注解v3.4版本新增strategy属性

### Id生成策略

从3.4版本开始,@Id注解带有strategy属性,strategy的值为以下值之一

* None(用户自己手动设置该Id属性)
* AutoIncrement(设置为数据库自增,默认值)
* IdGenerator(使用Id生成器,需要在配置DAO对象时手动指定Id生成器)

每个实体类可以单独设置自己的Id生成策略,同时也可以在配置DAO对象时指定全局Id策略.@Id注解优先于全局策略.

全局Id生成器策略请参阅[配置DAO](/zh-cn/config/configuration.md)

## @ColumnNamne

映射字段名,作用于类字段上

```java
public class User{
    @ColumnName("uid")
    private long id;
}
```

## @TableName

映射表名,作用于类上

```java
@TableName("myUser")
public class User{
}
```

## @ColumnType

自定义数据库类型,作用于类字段上

```java
public class User{
    @ColumnName("varchar(1024)")
    private String username;
}
```

## @Comment

添加数据库注释,作用于类字段上

```java
public class User{
    @Comment("用户名")
    private String username;
}
```

## @Index

在该字段上建立索引,作用于类字段上

> 建议在有唯一性约束的字段上建立索引,以便加快检索速度

```java
public class User{
    @Index
    private String username;
}
```

## @Constraint

添加约束,包括是否非空,是否唯一,check约束以及默认值,作用于类字段上

* notNull 是否非空,默认为false
* unique 是否唯一,默认为false
* check check约束,默认为空
* defaultValue 默认值,默认为为空

> unionUnique属性从v3.3版本开始提供

* unionUnique 是否建立联合唯一约束,默认为true

建立唯一约束时,会将所有unionUnique属性为true的字段建立一个联合唯一约束.
对于unionUnique为false的字段会单独建立唯一约束

```java
public class User{
    //userId属性id必须大于0
    @Constraint(check = "#{userId} > 0")
    private String userId;
 
    //设置username属性非空且唯一
    @Constraint(notNull=true, unique=true)
    private String username;

    //年龄默认设置为5岁
    @Constraint(defaultValue="5")
    private int age;
}
```

## @ForeignKey

添加外键约束,指定关联字段以及外键字段更新和删除时的策略,作用于类字段上

* table 关联到哪张表
* field 关联到表的哪个字段,默认为id
* foreignKeyOption 外键级联更新策略,默认为NOACTION

```java
public class User{ 
    @ForeignKey(table=Address.class,field="uid",foreignKeyOption=ForeignKeyOption.RESTRICT)
    private long addressId;
}
```

## @TableField

> 此注解从3.2版本开始提供

添加表字段信息,指定信息后框架会自动注入当前时间值

* createdAt 是否填充插入时间,默认为false
* updatedAt 是否填充更新时间,默认为false

```java
@TableField(createdAt = true)
private Date createdAt;

@TableField(updatedAt = true)
private Date updatedAt;
```

> v3.5新增function属性

指定了function属性后,在查询,新增和更新操作时,会自动用指定的function值替换对应的SQL语句部分.

场景举例: 密码字段使用md5函数加密

```java
//以下注解会使得在查询,插入,更新此字段时都会自动执行md5函数
@TableField(function = "md5(concat('salt#',#{password}))")
private String password;
```
## @Ignore

忽略该类/忽略该字段,作用于类和类字段上

忽略age属性

```java
public class User{
    @Ignore
    private int age;
}
```

忽略Address类

```java
@Ignore
public class Address{
}
```

> 默认情况下QuickDAO会自动忽略实体类中集合类成员变量以及实体类成员变量,除非您调用了Condition类的compositField方法

```java
public class User{
    private Address address;
    private List<Long> userIds;
    //默认情况下上述属性均会自动被QuickDAO所忽略
}
```
