package cn.schoolwow.quickdao.dao.sql.transaction;

import cn.schoolwow.quickdao.dao.sql.dml.DMLDAO;

import java.sql.Savepoint;

/**事务接口*/
public interface Transaction extends DMLDAO {
    /**
     * 设置保存点
     */
    Savepoint setSavePoint(String name);

    /**
     * 事务回滚
     */
    void rollback();

    /**
     * 事务回滚
     */
    void rollback(Savepoint savePoint);

    /**
     * 事务提交
     */
    void commit();

    /**
     * 结束事务
     */
    void endTransaction();
}
