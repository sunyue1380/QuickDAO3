package cn.schoolwow.quickdao.builder.table;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;

import java.sql.SQLException;
import java.util.List;

/**负责数据库表创建*/
public interface TableBuilder{
    /**获取数据库名称*/
    String getDatabaseName() throws SQLException;
    /**获取数据库信息*/
    List<Entity> getDatabaseEntity() throws SQLException;
    /**创建自增列SQL语句*/
    String getAutoIncrementSQL(Property property) throws SQLException;
    /**判断表是否已经存在*/
    boolean hasTableExists(Entity entity) throws SQLException;
    /**创建新表*/
    void createTable(Entity entity) throws SQLException;
    /**创建新表*/
    void createProperty(Property property) throws SQLException;
    /**修改列*/
    void alterColumn(Property property) throws SQLException;
    /**删除列*/
    void deleteColumn(Property property) throws SQLException;
    /**删除表*/
    void dropTable(String tableName) throws SQLException;
    /**重建表*/
    void rebuild(Entity entity) throws SQLException;
    /**判断索引是否已经存在*/
    boolean hasIndexExists(Entity entity,IndexType indexType) throws SQLException;
    /**
     * 创建索引
     * @param entity 实体表信息
     * @param indexType 索引类型
     * */
    void createIndex(Entity entity,IndexType indexType) throws SQLException;
    /**判断外键约束是否已经存在*/
    boolean hasConstraintExists(String tableName,String constraintName) throws SQLException;
    /**删除索引*/
    void dropIndex(Entity entity,IndexType indexType) throws SQLException;
    /**建立外键约束*/
    void createForeignKey(Property property) throws SQLException;
    /**自动建表和新增字段*/
    void automaticCreateTableAndField() throws SQLException;
    /**刷新数据库字段信息*/
    void refreshDbEntityList();
}
