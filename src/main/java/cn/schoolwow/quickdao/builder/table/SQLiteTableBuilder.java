package cn.schoolwow.quickdao.builder.table;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SQLiteTableBuilder extends AbstractTableBuilder{
    public SQLiteTableBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
        fieldMapping.put("long", "INTEGER");
    }

    @Override
    public Entity[] getDatabaseEntity() throws SQLException {
        PreparedStatement tablePs = connection.prepareStatement("select name from sqlite_master where type='table';");
        ResultSet tableRs = tablePs.executeQuery();
        List<Entity> entityList = new ArrayList<>();
        while (tableRs.next()) {
            Entity entity = new Entity();
            entity.tableName = tableRs.getString(1);

            List<Property> propertyList = new ArrayList<>();
            //获取所有列
            {
                ResultSet propertiesRs = connection.prepareStatement("PRAGMA table_info(`" + entity.tableName + "`)").executeQuery();
                while (propertiesRs.next()) {
                    Property property = new Property();
                    property.column = propertiesRs.getString("name");
                    property.columnType = propertiesRs.getString("type");
                    property.notNull = "1".equals(propertiesRs.getString("notnull"));
                    if (null != propertiesRs.getString("dflt_value")) {
                        property.defaultValue = propertiesRs.getString("dflt_value");
                    }
                    propertyList.add(property);
                }
                propertiesRs.close();
            }
            //获取索引信息
            {
                ResultSet resultSet = connection.prepareStatement("SELECT sql FROM sqlite_master WHERE type='index' and tbl_name = '"+entity.tableName+"';").executeQuery();
                while (resultSet.next()) {
                    //判断是普通索引还是唯一性约束
                    String sql = resultSet.getString(1).toLowerCase();
                    String[] columns = sql.substring(sql.indexOf("(")+1,sql.indexOf(")")).split(",");
                    if(sql.contains("unique index")){
                        for(String column:columns){
                            for(Property property:propertyList){
                                if(property.column.equals(column.substring(1,column.length()-1))){
                                    property.unique = true;
                                    break;
                                }
                            }
                        }
                    }else{
                        for(String column:columns){
                            for(Property property:propertyList){
                                if(property.column.equals(column.substring(1,column.length()-1))){
                                    property.index = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                resultSet.close();
            }
            entity.properties = propertyList.toArray(new Property[0]);
            entityList.add(entity);
        }
        tableRs.close();
        tablePs.close();
        return entityList.toArray(new Entity[0]);
    }

    @Override
    public String getAutoIncrementSQL(Property property){
        return property.column + " " + property.columnType + " primary key autoincrement";
    }

    @Override
    public boolean hasTableExists(Entity entity) throws SQLException {
        ResultSet resultSet = connection.prepareStatement("select name from sqlite_master where type='table' and name = '"+entity.tableName+"';").executeQuery();
        boolean result = false;
        if(resultSet.next()){
            result = true;
        }
        resultSet.close();
        return result;
    }

    @Override
    public boolean hasIndexExists(Entity entity, IndexType indexType) throws SQLException {
        String indexName = entity.tableName+"_"+indexType.name();
        String sql = "select count(1) from sqlite_master where type = 'index' and name = '"+indexName+"'";
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
