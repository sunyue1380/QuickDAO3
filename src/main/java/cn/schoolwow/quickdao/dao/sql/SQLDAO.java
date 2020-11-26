package cn.schoolwow.quickdao.dao.sql;

import cn.schoolwow.quickdao.dao.condition.Condition;

import java.util.Collection;

public interface SQLDAO {
    /**
     * 实例对象是否存在
     * @param instance 实例对象
     */
    boolean exist(Object instance);
    /**
     * 是否数据库中存在任意一个示例对象数组内的对象
     * @param instances 实例对象数组
     */
    boolean existAny(Object... instances);
    /**
     * 是否数据库中存在示例对象数组内的所有对象
     * @param instances 实例对象数组
     */
    boolean existAll(Object... instances);
    /**
     * 是否数据库中存在任意一个示例对象数组内的对象
     * @param instances 实例对象数组
     */
    boolean existAny(Collection instances);
    /**
     * 是否数据库中存在示例对象数组内的所有对象
     * @param instances 实例对象数组
     */
    boolean existAll(Collection instances);
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
