package cn.schoolwow.quickdao.dao.sql.dql;

import cn.schoolwow.quickdao.dao.condition.Condition;
import cn.schoolwow.quickdao.dao.sql.SQLDAO;

import java.util.List;

/**
 * DQL查询接口
 * */
public interface DQLDAO extends SQLDAO {
    /**
     * 根据id查询实例
     * @param clazz 实例类对象
     * @param id 待查询id值
     */
    <T> T fetch(Class<T> clazz, long id);

    /**
     * 根据属性查询单个记录
     * @param clazz 实例类对象
     * @param field 指定字段名
     * @param value 指字段值
     */
    <T> T fetch(Class<T> clazz, String field, Object value);

    /**
     * 根据属性查询多个记录
     * @param clazz 实例类对象
     * @param field 指定字段名
     * @param value 指字段值
     */
    <T> List<T> fetchList(Class<T> clazz, String field, Object value);

    /**
     * 复杂查询
     * @param clazz 实体类表
     * */
    Condition query(Class clazz);

    /**
     * 复杂查询
     * @param tableName 指定表名
     * */
    Condition query(String tableName);

    /**
     * 添加FROM子查询
     * @param condition 子查询
     * */
    Condition query(Condition condition);
}
