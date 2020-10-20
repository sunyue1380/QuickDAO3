# 实体类生成

> 此功能从v3.6版本开始提供

QuickDAO支持扫描数据库表信息并自动生成对应Entity类功能

```java
BasicDataSource dataSource = new BasicDataSource();
dataSource.setDriverClassName("com.mysql.jdbc.Driver");
dataSource.setUrl("jdbc:mysql://127.0.0.1:3306/quickdao?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowMultiQueries=true&zeroDateTimeBehavior=convertToNull");
dataSource.setUsername("root");
dataSource.setPassword("123456");
DAO dao = QuickDAO.newInstance()
        //指定实体类的包名,该包名作为生成实体类的包名
        .packageName("com.mtq.entity")
        //指定DataSource        
        .dataSource(dataSource)
        .autoCreateTable(false)
        .autoCreateProperty(false)
        .build();
//指定Java源码所在文件夹,生成所有数据库表对应的实体类
dao.generateEntityFile("/path/to/src/main/java/",null);
//生成指定表对应的实体类
dao.generateEntityFile("/path/to/src/main/java/",new String[]{"person"});
```