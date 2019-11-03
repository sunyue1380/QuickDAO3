package cn.schoolwow.quickdao.database;

/**数据库信息*/
public interface Database {
    /**返回注释语句*/
    String comment(String comment);
    /**转义表,列等*/
    String escape(String value);
}
