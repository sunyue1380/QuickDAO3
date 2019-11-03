package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.dao.condition.Condition;
import cn.schoolwow.quickdao.dao.sql.dml.DMLDAO;
import cn.schoolwow.quickdao.dao.sql.dql.DQLDAO;
import cn.schoolwow.quickdao.dao.sql.transaction.Transaction;

/**数据库操作接口*/
public interface DAO extends DQLDAO,DMLDAO {
    /**
     * 复杂查询
     * */
    Condition query(Class clazz);

    /**
     * 开启事务
     */
    Transaction startTransaction();

    /**
     * 建表
     */
    void create(Class clazz);

    /**
     * 删表
     */
    void drop(Class clazz);

    /**
     * 重建表
     */
    void rebuild(Class clazz);
}
