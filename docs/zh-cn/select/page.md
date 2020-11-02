# 分页与排序

QuickDAO提供了非常简单的分页排序接口.

```java
Condition condition = dao.query(Person.class)
       //分页,第几页和每页个数
       .page(1,10)
       //分页,偏移量和返回个数
       .limit(0,10)
       //根据该字段升序排列
       .orderBy("id")
       //根据该字段升序排列
       .orderByDesc("id");
Response response = condition.execute();
PageVo<Person> personList = response.getPagingList();
```

## PageVo定义
```java
public class PageVo<T> implements Serializable {
    /**列表*/
    private List<T> list;
    /**总记录数*/
    private long totalSize;
    /**总页数*/
    private int totalPage;
    /**每页个数*/
    private int pageSize;
    /**当前页*/
    private int currentPage;
    /**是否还有下一页*/
    private boolean hasMore;
}
```