package cn.schoolwow.quickdao.builder.sql.dql;

import cn.schoolwow.quickdao.domain.Query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface DQLSQLBuilder {
    /**is null查询*/
    PreparedStatement fetchNull(Class clazz, String field) throws SQLException;

    /**根据id查询*/
    PreparedStatement fetch(Class clazz, long id) throws SQLException;

    /**根据字段值查询*/
    PreparedStatement fetch(Class clazz, String field, Object value) throws SQLException;

    /**
     * 获取符合条件的总数目
     */
    PreparedStatement count(Query query) throws SQLException;

    /**
     * 插入记录
     */
    PreparedStatement insert(Query query) throws SQLException;

    /**
     * 更新符合条件的记录
     */
    PreparedStatement update(Query query) throws SQLException;

    /**
     * 删除符合条件的数据库记录
     */
    PreparedStatement delete(Query query) throws SQLException;

    /**
     * 返回符合条件的数据库记录
     */
    PreparedStatement getArray(Query query) throws SQLException;
}
