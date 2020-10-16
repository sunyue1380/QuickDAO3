package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.dao.sql.dml.DMLDAO;
import cn.schoolwow.quickdao.dao.sql.dql.DQLDAO;
import cn.schoolwow.quickdao.dao.sql.transaction.Transaction;
import cn.schoolwow.quickdao.domain.Entity;

import javax.sql.DataSource;
import java.util.Map;

/**数据库操作接口*/
public interface DAO extends DQLDAO,DMLDAO {
    /**
     * 开启事务
     */
    Transaction startTransaction();

    /**
     * 表是否存在
     * @param tableName 表名
     */
    boolean hasTable(String tableName);

    /**
     * 建表
     */
    void create(Class clazz);

    /**
     * 建表
     */
    void create(Entity entity);

    /**
     * 删表
     */
    void drop(Class clazz);

    /**
     * 删表
     */
    void drop(String tableName);

    /**
     * 重建表
     */
    void rebuild(Class clazz);

    /**
     * 获取连接池
     */
    DataSource getDataSource();

    /**
     * 获取扫描的所有实体类信息
     */
    Map<String, Entity> getEntityMap();

    /**
     * 获取数据库信息
     */
    Entity[] getDbEntityList();

    /**
     * 生成entity的java文件
     * @param tableNames 指定需要生成实体类的对应的表名
     */
    void generateEntityFile(String sourcePath, String[] tableNames);
}
