package cn.schoolwow.quickdao.builder.sql.dml;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface DMLSQLBuilder {
    /**插入语句*/
    PreparedStatement insert(Object instance) throws Exception;
    /**根据id更新*/
    PreparedStatement insert(Object[] instances) throws Exception;
    /**根据唯一性约束更新*/
    PreparedStatement updateByUniqueKey(Object instance) throws Exception;
    /**根据唯一性约束更新*/
    PreparedStatement updateByUniqueKey(Object[] instances) throws Exception;
    /**根据id更新*/
    PreparedStatement updateById(Object instance) throws Exception;
    /**根据id更新*/
    PreparedStatement updateById(Object[] instances) throws Exception;
    /**根据字段值删除*/
    PreparedStatement deleteByProperty(Class clazz, String property, Object value) throws SQLException;
    /**清空表*/
    PreparedStatement clear(Class clazz) throws SQLException;
}
