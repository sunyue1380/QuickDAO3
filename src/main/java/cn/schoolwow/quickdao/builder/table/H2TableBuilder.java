package cn.schoolwow.quickdao.builder.table;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;

import java.sql.ResultSet;
import java.sql.SQLException;

public class H2TableBuilder extends MySQLTableBuilder{

    public H2TableBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
        fieldMapping.put("long", "BIGINT");
        fieldMapping.put("float", "REAL");
        fieldMapping.put("double", "DOUBLE");
    }

    @Override
    public boolean hasTableExists(Entity entity) throws SQLException {
        ResultSet resultSet = connection.prepareStatement("select table_name from information_schema.tables where table_name = '"+entity.tableName.toUpperCase()+"'").executeQuery();
        boolean result = false;
        if(resultSet.next()){
            result = true;
        }
        resultSet.close();
        return result;
    }

    @Override
    public boolean hasIndexExists(Entity entity, IndexType indexType) throws SQLException {
        String indexName = (entity.tableName+"_"+indexType.name()).toUpperCase();
        String sql = "select count(1) from information_schema.indexes where index_name = '"+indexName+"'";
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
