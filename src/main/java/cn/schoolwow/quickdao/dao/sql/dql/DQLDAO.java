package cn.schoolwow.quickdao.dao.sql.dql;

import cn.schoolwow.quickdao.dao.sql.SQLDAO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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
     * 根据属性查询单个记录
     * @param tableName 表名
     * @param field 指定字段名
     * @param value 指字段值
     */
    JSONObject fetch(String tableName, String field, Object value);

    /**
     * 根据属性查询多个记录
     * @param tableName 表名
     * @param field 指定字段名
     * @param value 指字段值
     */
    JSONArray fetchList(String tableName, String field, Object value);
}
