# 配置DAO对象

QuickDAO通过配置信息生成DAO对象以便对数据库进行操作.用户可根据实际需求设置相应配置信息

## 配置DAO对象

```java
//QuickDAO需要传递DataSource实现对象,您可以自由选择市面上的任意DataSource实现,本例采用dbcp
        BasicDataSource mysqlDataSource = new BasicDataSource();
        mysqlDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        mysqlDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/quickdao");
        mysqlDataSource.setUsername("root");
        mysqlDataSource.setPassword("123456");
        DAO dao = QuickDAO.newInstance()
                //指定DataSource
                .dataSource(mysqlDataSource)
                //指定要扫描的实体类包,支持多级目录
                .packageName("cn.schoolwow.quickdao.entity")
                //指定要扫描的实体类包,支持多级目录,同时添加表名前缀quickdao
                .packageName("cn.schoolwow.quickdao.entity","quickdao")
                //指定扫描单个实体类(v3.2版本开始提供)
                .entity(Person.class)
                //是否自动新增表,默认开启
                .autoCreateTable(false)
                //是否自动新增字段,默认开启
                .autoCreateProperty(false)
                //指定全局Id策略(v3.4版本开始提供)
                .idStrategy(IdStrategy.IdGenerator)
                //指定全局Id生成期实例(目前只支持雪花算法)
                .idGenerator(new SnowflakeIdGenerator())
                //是否建表时建立外键约束,默认关闭
                .foreignKey(false)
                //忽略指定实体类
                .ignoreClass(Person.class)
                //忽略指定实体类包
                .ignorePackageName("cn.schoolwow.quickdao.entity.ignore")
                //返回DAO接口对象
                .build();
```

## 动态定义实体类注解

QuickDAO指定动态指定实体类注解,此功能适用于扫描第三方实体类包,即无法编辑实体类源码时.

```java
DAO dao = QuickDAO.newInstance()
                .define(Person.class)
                .tableName("p")
                .property("lastName")
                .notNull(true)
                .unique(true)
                .defaultValue("quickdao")
                .done()
                .comment("Person表")
                .done()
                .build();
```

## 指定全局Id策略

> 此功能从v3.4版本新增

您可以指定全局Id生成策略,该全局策略对所有使用@Id注解的字段起作用

```java
public class User{
  @Id
  private long id;
}

QuickDAO.newInstance()
              //指定全局Id策略
              .idStrategy(IdStrategy.IdGenerator)
              .done();
```

> 若手动使用@Id注解设置了strategy属性,则使用@Id注解所设置的策略

QuickDAO内置了SnowflakeIdGenerator生成器,您也可以通过实现IdGenerator接口自定义Id生成器

> id生成器只对所有用@Id注解的属性起效果,请务必在id属性上添加@Id注解

