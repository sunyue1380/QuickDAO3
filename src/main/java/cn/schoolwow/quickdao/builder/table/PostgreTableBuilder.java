package cn.schoolwow.quickdao.builder.table;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgreTableBuilder extends AbstractTableBuilder {
    public PostgreTableBuilder(QuickDAOConfig quickDAOConfig){
        super(quickDAOConfig);
        fieldMapping.put("byte", "smallint");
        fieldMapping.put("date", "timestamp");
        fieldMapping.put("calendar", "timestamp");
        fieldMapping.put("localdate", "timestamp");
        fieldMapping.put("localdatetime", "timestamp");
        fieldMapping.put("float", "real");
        fieldMapping.put("double", "double precision");
    }

    @Override
    public Entity[] getDatabaseEntity() throws SQLException {
        PreparedStatement tablePs = connection.prepareStatement("select tablename from pg_tables where schemaname='public';");
        ResultSet tableRs = tablePs.executeQuery();
        List<Entity> entityList = new ArrayList<>();
        while (tableRs.next()) {
            Entity entity = new Entity();
            entity.tableName = tableRs.getString(1);

            List<Property> propertyList = new ArrayList<>();
            //获取列
            {
                ResultSet propertiesRs = connection.prepareStatement("select column_name,column_default,is_nullable,udt_name from information_schema.columns where table_name = '" + entity.tableName + "'").executeQuery();
                while (propertiesRs.next()) {
                    Property property = new Property();
                    property.column = propertiesRs.getString("column_name");
                    property.columnType = propertiesRs.getString("udt_name");
                    property.notNull = "NO".equals(propertiesRs.getString("is_nullable"));
                    if (null != propertiesRs.getString("column_default")) {
                        property.defaultValue = propertiesRs.getString("column_default");
                    }
                    propertyList.add(property);
                }
            }
            //获取索引信息
            updateTableIndex("select indexdef from pg_indexes where tablename='"+entity.tableName+"';",propertyList);
            entity.properties = propertyList.toArray(new Property[0]);
            entityList.add(entity);
        }
        tableRs.close();
        tablePs.close();
        return entityList.toArray(new Entity[0]);
    }

    @Override
    public String getAutoIncrementSQL(Property property) {
        return property.column + " SERIAL unique";
    }

    @Override
    public boolean hasTableExists(Entity entity) throws SQLException {
        ResultSet resultSet = connection.prepareStatement("select tablename from pg_tables where schemaname='public' and tablename = '"+entity.tableName+"';").executeQuery();
        boolean result = false;
        if(resultSet.next()){
            result = true;
        }
        resultSet.close();
        return result;
    }

    @Override
    public void createTable(Entity entity) throws SQLException {
        super.createTable(entity);
        //创建注释
        String entityCommentSQL = "comment on table \"" + entity.tableName + "\" is '" + entity.comment + "'";
        connection.prepareStatement(entityCommentSQL).executeUpdate();
        for (Property property : entity.properties) {
            if (property.comment == null) {
                continue;
            }
            String columnCommentSQL = "comment on column \"" + entity.tableName + "\".\"" + property.column + "\" is '" + property.comment + "'";
            connection.prepareStatement(columnCommentSQL).executeUpdate();
        }
    }

    @Override
    public boolean hasIndexExists(Entity entity, IndexType indexType) throws SQLException {
        String indexName = entity.tableName+"_"+indexType.name();
        String sql = "select count(1) from pg_indexes where tablename = '"+entity.tableName+"' and indexname = '"+indexName+"'";
        logger.trace("[查看索引是否存在]表名:{},执行SQL:{}",entity.tableName,sql);
        ResultSet resultSet = connection.prepareStatement(sql).executeQuery();
        boolean result = false;
        if (resultSet.next()) {
            result = resultSet.getInt(1) > 0;
        }
        resultSet.close();
        return result;
    }
}
