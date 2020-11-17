package cn.schoolwow.quickdao.dao.sql;

import cn.schoolwow.quickdao.dao.condition.Condition;

public interface SQLDAO {
    /**
     * 实例对象是否存在
     * @param instance 实例对象
     */
    boolean exist(Object instance);
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
