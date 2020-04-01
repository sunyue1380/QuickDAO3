# 简单操作

QuickDAO提供简单的增删操作

* ``dao.insert(user);``

插入user实例

* ``dao.insert(users);``

插入user数组

* ``dao.update(user);``

更新user实例,若存在唯一性约束,则根据唯一性约束更新
若存在id,则根据id更新,
若都不存在,则忽略更新操作

* ``dao.update(users);``

更新user数组实例

* ``dao.save(user);``

保存user实例,判断User实例是否已经存在在数据库中,存在则更新,不存在则插入

* ``dao.save(users);``

保存user数组
