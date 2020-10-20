# QuickDAO3

QuickDAO是一个简单易用方便的Java ORM框架.具有以下优势:

* 只需要注入一个DAO即可完成初始化操作
* 自动建表,自动新增数据库字段
* API层面支持外键关联查询,支持复杂的外键关联查询
* 内置数据库方言支持
* 实体类注解,支持自定义字段名称,类型,是否建立索引,建立外键关联

# 支持数据库

* MySQL(5.0以上)
* SQLite
* H2
* Postgre(9.0.0以上)
* SQL Server(2012版本以上)

# 更新日志

[点此访问](http://quickdao.schoolwow.cn/)

# 快速入门

## 1 导入QuickDAO
QuickDAO基于JDBC,为提高效率,默认只支持数据库连接池.

* 导入commons-dbcp(或者其他的DataSource实现)
* 导入QuickDAO最新版本
```
<dependency>
   <groupId>commons-dbcp</groupId>
   <artifactId>commons-dbcp</artifactId>
   <version>1.4</version>
</dependency>
<dependency>
  <groupId>cn.schoolwow</groupId>
  <artifactId>QuickDAO</artifactId>
  <version>3.6</version>
</dependency>
```

## 2 配置QuickDAO
```java
BasicDataSource mysqlDataSource = new BasicDataSource();
mysqlDataSource.setDriverClassName("com.mysql.jdbc.Driver");
mysqlDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/quickdao");
mysqlDataSource.setUsername("root");
mysqlDataSource.setPassword("123456");
//指定实体所在包名
cn.schoolwow.quickdao.dao.DAO dao = QuickDAO.newInstance()
                    .dataSource(mysqlDataSource)
                    .packageName("cn.schoolwow.quickdao.entity")
                    .build();
//之后所有的操作使用dao对象完成
```

## 3使用QuickDAO

* 根据id查询

```User user = dao.fetch(User.class,1);```

* 根据单个属性查询

```User user = dao.fetch(User.class,"username","quickdao");```

* 插入对象

```dao.insert(user);```

* 更新对象

```dao.update(user);```

* 保存对象(存在则更新,不存在则插入)

```dao.save(user);```

* 根据id删除

```dao.delete(User.class,1);```

* 根据属性值删除

```dao.delete(User.class,"username","quickdao");```

* 复杂查询
```java
List<User> userList = dao.query(User.class)
    .addQuery("name","quickdao")
    .addNotNullQuery("password")
    .page(1,10)
    .orderBy("id")
    .execute()
    .getList();
```
* 外键关联查询
```java
List<User> userList = dao.query(User.class)
    joinTable(Address.class,"addressId","id")
    .addQuery("name","BeiJing")
    .done()
    .addQuery("name","quickdao")
    .page(1,10)
    .orderBy("id")
    .compositField()
    .execute()
    .getList();
```

# 详细文档

### [点此访问](https://sunyue1380.github.io/QuickDAO3)

# 反馈

目前QuickDAO还不成熟,还在不断完善中.若有问题请提交Issue或者发送邮件到648823596@qq.com,作者将第一时间跟进并努力解决.同时欢迎热心认识提交PR,共同完善QuickDAO项目!

# 开源协议
本软件使用 [GPL](http://www.gnu.org/licenses/gpl-3.0.html) 开源协议!
