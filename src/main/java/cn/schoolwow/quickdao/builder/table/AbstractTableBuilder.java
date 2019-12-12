package cn.schoolwow.quickdao.builder.table;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public abstract class AbstractTableBuilder implements TableBuilder{
    protected Logger logger = LoggerFactory.getLogger(TableBuilder.class);
    /**字段类型映射*/
    protected Map<String, String> fieldMapping = new HashMap<String, String>();
    /**数据库连接*/
    public Connection connection;
    /**数据库配置项*/
    public QuickDAOConfig quickDAOConfig;

    public AbstractTableBuilder(QuickDAOConfig quickDAOConfig) {
        this.quickDAOConfig = quickDAOConfig;
        fieldMapping.put("string", "varchar(255)");
        fieldMapping.put("boolean", "boolean");
        fieldMapping.put("byte", "tinyint");
        fieldMapping.put("char", "char");
        fieldMapping.put("short", "smallint");
        fieldMapping.put("int", "integer");
        fieldMapping.put("integer", "integer");
        fieldMapping.put("long", "bigint");
        fieldMapping.put("float", "float");
        fieldMapping.put("double", "double");
        fieldMapping.put("date", "datetime");
        fieldMapping.put("time", "time");
        fieldMapping.put("timestamp", "timestamp");
    }

    public abstract Entity[] getDatabaseEntity() throws SQLException;

    public abstract String getAutoIncrementSQL(Property property);

    public abstract boolean hasTableExists(Entity entity) throws SQLException;

    @Override
    public void createTable(Entity entity) throws SQLException {
        StringBuilder createTableBuilder = new StringBuilder("create table " + quickDAOConfig.database.escape(entity.tableName) + "(");
        Property[] properties = entity.properties;
        for (Property property : properties) {
            if(property.id){
                createTableBuilder.append(getAutoIncrementSQL(property));
            }else{
                createTableBuilder.append(quickDAOConfig.database.escape(property.column) + " " + property.columnType);
                if (property.notNull) {
                    createTableBuilder.append(" not null");
                }
                if (null!=property.defaultValue&&!property.defaultValue.isEmpty()) {
                    createTableBuilder.append(" default " + property.defaultValue);
                }
                if (null != property.comment) {
                    createTableBuilder.append(" "+quickDAOConfig.database.comment(property.comment));
                }
                if (null!=property.check&&!property.check.isEmpty()) {
                    createTableBuilder.append(" check " + property.check);
                }
            }
            createTableBuilder.append(",");
        }
        if (quickDAOConfig.openForeignKey&&null!=entity.foreignKeyProperties&&entity.foreignKeyProperties.length>0) {
            if(this instanceof SQLiteTableBuilder){
                //手动开启外键约束
                connection.prepareStatement("PRAGMA foreign_keys = ON;").executeUpdate();
            }
            for (Property property : entity.foreignKeyProperties) {
                createTableBuilder.append("foreign key(" + quickDAOConfig.database.escape(property.column) + ") references ");
                String operation = property.foreignKey.foreignKeyOption().getOperation();
                createTableBuilder.append(quickDAOConfig.database.escape(quickDAOConfig.entityMap.get(property.foreignKey.table().getName()).tableName) + "(" + quickDAOConfig.database.escape(property.foreignKey.field()) + ") ON DELETE " + operation+ " ON UPDATE " + operation);
                createTableBuilder.append(",");
            }
        }
        createTableBuilder.deleteCharAt(createTableBuilder.length() - 1);
        createTableBuilder.append(")");
        if (null != entity.comment) {
            createTableBuilder.append(" "+quickDAOConfig.database.comment(entity.comment));
        }
        logger.debug("[生成新表]类名:{},表名:{},执行SQL:{}", entity.className, entity.tableName, createTableBuilder.toString());
        connection.prepareStatement(createTableBuilder.toString()).executeUpdate();
    }

    @Override
    public void alterColumn(Property property) throws SQLException{
        StringBuilder builder = new StringBuilder("alert table " + quickDAOConfig.database.escape(property.entity.tableName));
        builder.append(" alter column "+quickDAOConfig.database.escape(property.column)+" "+property.columnType);
        logger.debug("[修改数据类型]:类名:{},表名:{},列名:{},执行SQL:{}", property.entity.className, property.entity.tableName, property.column, builder.toString());
        connection.prepareStatement(builder.toString()).executeUpdate();
    }

    @Override
    public void dropTable(Entity entity) throws SQLException {
        String sql = "drop table "+quickDAOConfig.database.escape(entity.tableName);
        logger.debug("[删除表]类名:{},表名:{},执行SQL:{}", entity.className, entity.tableName, sql);
        connection.prepareStatement(sql).executeUpdate();
    }

    @Override
    public void rebuild(Entity entity) throws SQLException {
        if(hasTableExists(entity)){
            dropTable(entity);
        }
        createTable(entity);
        createIndex(entity,IndexType.Index);
        createIndex(entity,IndexType.Unique);
    }

    @Override
    public abstract boolean hasIndexExists(Entity entity,IndexType indexType) throws SQLException;

    @Override
    public void createIndex(Entity entity, IndexType indexType) throws SQLException {
        if(null==entity||null==indexType){
            return;
        }
        String indexName = entity.tableName+"_"+indexType.name();
        switch (indexType){
            case Index:{
                if (null == entity.indexProperties || entity.indexProperties.length == 0) {
                    return;
                }
                StringBuilder indexBuilder = new StringBuilder("create index " + quickDAOConfig.database.escape(indexName)+" on " + quickDAOConfig.database.escape(entity.tableName) + " (");
                for (Property property : entity.indexProperties) {
                    indexBuilder.append(quickDAOConfig.database.escape(property.column)+",");
                }
                indexBuilder.deleteCharAt(indexBuilder.length() - 1);
                indexBuilder.append(");");
                logger.debug("[添加索引]表:{},执行SQL:{}", entity.tableName, indexBuilder.toString());
                connection.prepareStatement(indexBuilder.toString()).executeUpdate();
            }break;
            case Unique:{
                if (null == entity.uniqueKeyProperties || entity.uniqueKeyProperties.length == 0) {
                    return;
                }
                StringBuilder indexUniqueBuilder = new StringBuilder("create unique index " + quickDAOConfig.database.escape(indexName)+" on " + quickDAOConfig.database.escape(entity.tableName) + " (");
                for (Property property : entity.uniqueKeyProperties) {
                    indexUniqueBuilder.append(quickDAOConfig.database.escape(property.column)+",");
                }
                indexUniqueBuilder.deleteCharAt(indexUniqueBuilder.length() - 1);
                indexUniqueBuilder.append(");");
                logger.debug("[添加唯一性约束]表:{},执行SQL:{}", entity.tableName, indexUniqueBuilder.toString());
                connection.prepareStatement(indexUniqueBuilder.toString()).executeUpdate();
            }break;
        }
    }

    @Override
    public boolean hasConstraintExists(String tableName, String constraintName) throws SQLException {
        ResultSet resultSet = connection.prepareStatement("select count(1) from information_schema.KEY_COLUMN_USAGE where constraint_name='" + constraintName + "'").executeQuery();
        boolean result = false;
        if (resultSet.next()) {
            result = resultSet.getInt(1) > 0;
        }
        resultSet.close();
        return result;
    }

    @Override
    public void dropIndex(Entity entity, IndexType indexType) throws SQLException{
        String indexName = entity.tableName+"_"+indexType.name();
        String dropIndexSQL = "drop index "+quickDAOConfig.database.escape(indexName);
        logger.debug("[删除索引]表:{},执行SQL:{}", entity.tableName, dropIndexSQL);
        connection.prepareStatement(dropIndexSQL).executeUpdate();
    }

    public void autoBuildDatabase() throws SQLException {
        //对比实体类信息与数据库信息
        Entity[] dbEntityList = getDatabaseEntity();
        logger.debug("[获取数据库信息]数据库表个数:{}", dbEntityList.length);
        //确定需要新增的表和更新的表
        Collection<Entity> entityList = quickDAOConfig.entityMap.values();
        List<Entity> newEntityList = new ArrayList<>();
        List<Entity> updateEntityList = new ArrayList<>();
        for (Entity entity : entityList) {
            for(Property property:entity.properties){
                if(null==property.columnType||property.columnType.isEmpty()){
                    property.columnType = fieldMapping.get(property.simpleTypeName);
                }
                if(null!=property.check&&!property.check.isEmpty()){
                    property.check = property.check.replace("#{"+property.name+"}",quickDAOConfig.database.escape(property.column));
                    if(!property.check.contains("(")){
                        property.check = "("+property.check+")";
                    }
                }
            }
            for (Entity dbEntity : dbEntityList) {
                if (entity.tableName.toLowerCase().equals(dbEntity.tableName.toLowerCase())) {
                    updateEntityList.add(entity);
                    break;
                }
            }
            if(!updateEntityList.contains(entity)){
                newEntityList.add(entity);
            }
        }
        List<Entity> finalNewEntityList = new ArrayList<>(newEntityList.size());
        for(Entity entity:newEntityList){
            changeNewEntityCreateOrder(entity,finalNewEntityList);
        }
        if (quickDAOConfig.autoCreateTable) {
            for(Entity entity: newEntityList){
                createTable(entity);
                createIndex(entity,IndexType.Index);
                createIndex(entity,IndexType.Unique);
            }
        }
        if(quickDAOConfig.autoCreateProperty){
            //更新表
            for(Entity entity : updateEntityList){
                for (Entity dbEntity : dbEntityList) {
                    if (entity.tableName.equals(dbEntity.tableName)) {
                        compareEntityDatabase(entity,dbEntity);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 对比实体类和数据表并创建新列
     */
    private void compareEntityDatabase(Entity entity, Entity dbEntity) throws SQLException{
        Property[] entityProperties = entity.properties;
        Property[] dbEntityProperties = dbEntity.properties;
        boolean hasIndexProperty = false;
        boolean hasUniqueProperty = false;
        List<Property> foreignKeyPropertyList = new ArrayList<>();
        for (Property entityProperty : entityProperties) {
            boolean columnExist = false;
            for (Property dbEntityProperty : dbEntityProperties) {
                if (dbEntityProperty.column.equals(entityProperty.column)) {
                    if(entityProperty.id){
                        break;
                    }
                    columnExist = true;
                    //判断有无唯一性约束的改变
                    if(dbEntityProperty.unique != entityProperty.unique){
                        hasUniqueProperty = true;
                    }
                    //判断有无索引的改变
                    if(dbEntityProperty.index != entityProperty.index){
                        hasIndexProperty = true;
                    }
                    break;
                }
            }
            if (!columnExist) {
                addProperty(entityProperty);
                if(entityProperty.index){
                    hasIndexProperty = true;
                }
                if(entityProperty.unique){
                    hasUniqueProperty = true;
                }
                if(null!=entityProperty.foreignKey){
                    foreignKeyPropertyList.add(entityProperty);
                }
            }
        }
        //如果新增属性中有索引属性,则重新建立联合索引
        if (hasIndexProperty) {
            if(hasIndexExists(entity,IndexType.Index)){
                dropIndex(entity,IndexType.Index);
            }
            createIndex(entity,IndexType.Index);
        }
        //如果新增属性中有唯一约束,则重新建立联合唯一约束
        if (hasUniqueProperty) {
            if(hasIndexExists(entity,IndexType.Unique)){
                dropIndex(entity,IndexType.Unique);
            }
            createIndex(entity,IndexType.Unique);
        }
        //建立外键
        for(Property property:foreignKeyPropertyList){
            addForeignKey(property);
        }
    }

    /**
     * 表添加属性
     */
    private void addProperty(Property property) throws SQLException{
        StringBuilder addColumnBuilder = new StringBuilder();
        addColumnBuilder.append("alter table " + quickDAOConfig.database.escape(property.entity.tableName) + " add " + quickDAOConfig.database.escape(property.column) + " " + property.columnType);
        if (property.notNull) {
            addColumnBuilder.append(" not null");
        }
        if (null!=property.defaultValue&&!property.defaultValue.isEmpty()) {
            addColumnBuilder.append(" default " + property.defaultValue);
        }
        if (null!=property.check&&!property.check.isEmpty()) {
            addColumnBuilder.append(" check " + property.check);
        }
        if (null != property.comment) {
            addColumnBuilder.append(" "+quickDAOConfig.database.comment(property.comment));
        }
        addColumnBuilder.append(";");
        logger.debug("[添加新列]表:{},列名:{},执行SQL:{}", property.entity.tableName, property.column + "(" + property.columnType + ")", addColumnBuilder.toString());
        connection.prepareStatement(addColumnBuilder.toString()).executeUpdate();
    }

    /**
     * 表添加属性
     */
    private void addForeignKey(Property property) throws SQLException{
        String operation = property.foreignKey.foreignKeyOption().getOperation();
        String reference = quickDAOConfig.database.escape(quickDAOConfig.entityMap.get(property.foreignKey.table().getName()).tableName) + "(" + quickDAOConfig.database.escape(property.foreignKey.field()) + ") ON DELETE " + operation + " ON UPDATE " + operation;
        String foreignKeyName = "FK_" + property.entity.tableName + "_" + property.foreignKey.field() + "_" + quickDAOConfig.entityMap.get(property.foreignKey.table().getName()).tableName + "_" + property.name;
        if (hasConstraintExists(property.entity.tableName,foreignKeyName)) {
            return;
        }
        String foreignKeySQL = "alter table " + quickDAOConfig.database.escape(property.entity.tableName) + " add constraint " + quickDAOConfig.database.escape(foreignKeyName) + " foreign key(" + quickDAOConfig.database.escape(property.column) + ") references " + reference;
        logger.info("[生成外键约束]约束名:{},执行SQL:{}", foreignKeyName, foreignKeySQL);
        connection.prepareStatement(foreignKeySQL).executeUpdate();
    }

    /**
     * 根据外键依赖关系
     * @param entity 当前要创建的实体类
     * @param finalNewEntityList 最终实体类顺序
     * */
    private void changeNewEntityCreateOrder(Entity entity,List<Entity> finalNewEntityList){
        if(null!=entity.foreignKeyProperties&&entity.foreignKeyProperties.length>0){
            for(Property property : entity.foreignKeyProperties){
                changeNewEntityCreateOrder(quickDAOConfig.entityMap.get(property.foreignKey.table().getName()),finalNewEntityList);
            }
        }
        finalNewEntityList.add(entity);
    }
}
