# QuickDAO

QuickDAO is a Java ORM Framework which is quick and easy to use.

# document

[En](https://sunyue1380.github.io) | [中文](https://sunyue1380.github.io)

# feature

* strong powerful support for foreign key query and no need for any SQL statements
* abandon xml file and XXXMapper
* auto create table and auto insert new field
* sql dialect support
* entity annotation for defining your own field type, unique index and so on

# support database
* MySQL(5.0 above)
* SQLite
* H2
* Postgre(9.0.0 above)
* SQL Server(2012 above)

# quickstart

``talk is cheap,show me the code!``

* User.java

```java
public class User{
    private long id; //necessary
    private String username;
    private String password;
}
```

* pom.xml
```xml
<dependency>
   <groupId>commons-dbcp</groupId>
   <artifactId>commons-dbcp</artifactId>
   <version>1.4</version>
</dependency>
<dependency>
  <groupId>cn.schoolwow</groupId>
  <artifactId>QuickDAO</artifactId>
  <version>3.1</version>
</dependency>
```

* use QuickDAO

```java
BasicDataSource mysqlDataSource = new BasicDataSource();
mysqlDataSource.setDriverClassName("com.mysql.jdbc.Driver");
mysqlDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/quickdao");
mysqlDataSource.setUsername("root");
mysqlDataSource.setPassword("123456");
//configure the quickdao and specify entity package 
cn.schoolwow.quickdao.dao.DAO dao = QuickDAO.newInstance()
                    .dataSource(mysqlDataSource)
                    .packageName("cn.schoolwow.quickdao.entity")
                    .build();
//that's all. 
```

* some basic usecase

```
User user = dao.fetch(User.class,1);
User user = dao.fetch(User.class,"username","quickdao");
List<User> userList = dao.fetchList(User.class,"name","quickdao");
dao.insert(user);
dao.update(user);
dao.save(user);
dao.insert(user);
```

* complex query
```java
List<User> userList = dao.query(User.class)
    .addQuery("name","quickdao")
    .addNotNullQuery("password")
    .page(1,10)
    .orderBy("id")
    .execute()
    .getList();
```

* foreign key query
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

# feedback
please feel free to submit the issue and pull request to this project and i will reply as soon as possible.

# license
[GPL](http://www.gnu.org/licenses/gpl-3.0.html)
