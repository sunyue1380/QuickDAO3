package cn.schoolwow.quickdao.dao.sql.dml;

import cn.schoolwow.quickdao.dao.sql.SQLDAO;

import java.util.Collection;

/**
 * DML操作接口
 * */
public interface DMLDAO extends SQLDAO {
    /**
     * 插入对象
     * @param instance 待保存对象
     */
    int insert(Object instance);

    /**
     * 插入对象数组
     * @param instances 待保存对象数组
     */
    int insert(Object[] instances);

    /**
     * 插入对象集合
     * @param instanceCollection 待保存对象集合
     */
    int insert(Collection instanceCollection);

    /**
     * 更新对象
     * 若对象有唯一性约束,则根据唯一性约束更新,否则根据id更新
     * @param instance 待更新对象
     */
    int update(Object instance);

    /**
     * 更新对象
     * 若对象有唯一性约束,则根据唯一性约束更新,否则根据id更新
     * @param instances 待更新对象数组
     */
    int update(Object[] instances);

    /**
     * 更新对象
     * 若对象有唯一性约束,则根据唯一性约束更新,否则根据id更新
     * @param instanceCollection 待更新对象集合
     */
    int update(Collection instanceCollection);

    /**
     * <p>保存对象</p>
     * <ul>
     *     <li>若对象id不存在,则直接插入该对象</li>
     *     <li>若对象id存在,则判断该对象是否有唯一性约束,若有则根据唯一性约束更新</li>
     *     <li>若该对象无唯一性约束,则根据id更新</li>
     * </ul>
     * @param instance 待保存对象
     */
    int save(Object instance);

    /**
     * <p>保存对象数组</p>
     * <ul>
     *     <li>若对象id不存在,则直接插入该对象</li>
     *     <li>若对象id存在,则判断该对象是否有唯一性约束,若有则根据唯一性约束更新</li>
     *     <li>若该对象无唯一性约束,则根据id更新</li>
     * </ul>
     * @param instances 待保存对象数组
     */
    int save(Object[] instances);

    /**
     * <p>保存对象数组</p>
     * <ul>
     *     <li>若对象id不存在,则直接插入该对象</li>
     *     <li>若对象id存在,则判断该对象是否有唯一性约束,若有则根据唯一性约束更新</li>
     *     <li>若该对象无唯一性约束,则根据id更新</li>
     * </ul>
     * @param instanceCollection 待保存对象集合
     */
    int save(Collection instanceCollection);

    /**
     * 根据id删除记录
     * @param clazz 实体类对象,对应数据库中的一张表
     * @param id 待删除记录id
     */
    int delete(Class clazz, long id);

    /**
     * 根据id删除记录
     * @param clazz 实体类对象,对应数据库中的一张表
     * @param id 待删除记录id
     */
    int delete(Class clazz, String id);

    /**
     * 根据指定字段值删除对象
     * @param clazz 实体类对象,对应数据库中的一张表
     * @param field 指定字段名
     * @param value 指定字段值
     */
    int delete(Class clazz, String field, Object value);

    /**
     * 清空表
     * @param clazz 类名,对应数据库中的一张表
     */
    int clear(Class clazz);
}
