# 配置DAO对象

QuickDAO通过配置信息生成DAO对象以便对数据库进行操作.用户可根据实际需求设置相应配置信息

## 数据源

QuickDAO通过DataSource获取Connection对象,故配置DAO时需要传递DataSource的实现类.
您可以自由选择市面已有的DataSource实现,例如dbcp,c3p0,druid等等.

```java
BasicDataSource mysqlDataSource = new BasicDataSource();
mysqlDataSource.setDriverClassName("com.mysql.jdbc.Driver");
mysqlDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/quickdao");
mysqlDataSource.setUsername("root");
mysqlDataSource.setPassword("123456");
QuickDAO.newInstance().dataSource(mysqlDataSource);
```

## 配置项

* 指定数据源(*)

```java
dataSource("cn.schoolwow.quickdao.entity")
```

* 指定要扫描的包

```java
packageName("cn.schoolwow.quickdao.entity")
//扫描包并且为该包下的所有实体类表添加t_前缀
packageName("cn.schoolwow.quickdao.entity","t")
```

* 扫描单个实体类

> 3.2版本开始提供

```java
entity(User.class)
//扫描指定实体类并且添加表前缀u_
entity(User.class,"u")
```

* 忽略指定包,指定类

```java
ignorePackageName("cn.schoolwow.quickdao.entity")
.ignoreClass(User.class)
.filter(Predicate<Class> predicate)
```

* 是否建立数据库外键约束,默认为false

```java
foreignKey(true)
```

* 是否自动建表,默认为true

```java
autoCreateTable(true)
```

* 是否自动新增字段,默认为true

```java
autoCreateProperty(true)
```

## 动态定义实体类注解

使用场景: 当项目引入了第三方代码,不能直接在源码上添加实体类注解时,可使用define()方法实现动态设置实体注解.

> define方法设计采用了流式操作,done方法返回

```java
.define(User.class)
.property("username").unique(true).done()
.property("password").notNull(true).done()
.done()
``` 

