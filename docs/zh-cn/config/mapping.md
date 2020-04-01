# 实体类表名映射

QuickDAO采用驼峰式命名转下划线命名转化类名到表名.
对于多层包结构,采用@作为分隔符

假设实体类包结构如下
```
.
└── cn.schoolwow.quickdao.entity
    ├── User.java
    ├── PlayHistory.java
    └── user
        ├── User.java
        └── history
            └── LoginHistory.java
```

扫描后映射的数据库表名如下:

|类名|表名|
|---|---|
|cn.schoolwow.quickdao.entity.User|user|
|cn.schoolwow.quickdao.entity.PlayHistory|play_history|
|cn.schoolwow.quickdao.entity.user.User|user@user|
|cn.schoolwow.quickdao.entity.user.history.LoginHistory|user_history@login_history|

> 使用packageName("cn.schoolwow.quickdao.entity","t");指定表名前缀时,会在当前已经映射的表名基础上再在前面加上前缀.

