# 简单查询

QuickDAO提供了一些便捷方法.当您只是根据单个条件查询或者只需要返回单条结果时,这些方法是非常有用的.

* <T> T fetch(Class<T> clazz, long id);

根据id查询,例如``fetch(User.class,1)``

* <T> T fetch(Class<T> clazz, String field, Object value);

根据单个条件查询并且只返回结果列表的第一条,例如``fetch(User.class,"username","quickdao"")``

* <T> List<T> fetchList(Class<T> clazz, String field, Object value);

根据单个条件查询,例如 ``fetchList(User.class,"username","quickdao"")``
