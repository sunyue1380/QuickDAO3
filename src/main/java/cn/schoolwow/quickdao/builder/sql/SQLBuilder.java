package cn.schoolwow.quickdao.builder.sql;

import cn.schoolwow.quickdao.builder.sql.dql.*;
import cn.schoolwow.quickdao.database.*;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;

import java.sql.PreparedStatement;

public interface SQLBuilder {
    /**根据唯一性约束查询*/
    PreparedStatement selectCountById(Object instance) throws Exception;
    /**根据唯一性约束查询*/
    PreparedStatement selectCountByUniqueKey(Object instance) throws Exception;

    /**新建对应SQLBuilder实例*/
    static AbstractDQLSQLBuilder getDQLSQLBuilderInstance(QuickDAOConfig quickDAOConfig){
        AbstractDQLSQLBuilder dqlsqlBuilder = null;
        if(quickDAOConfig.database instanceof MySQLDatabase){
            dqlsqlBuilder = new MySQLDQLSQLBuilder(quickDAOConfig);
        }else if(quickDAOConfig.database instanceof SQLiteDatabase){
            dqlsqlBuilder = new SQLiteDQLSQLBuilder(quickDAOConfig);
        }else if(quickDAOConfig.database instanceof H2Database){
            dqlsqlBuilder = new H2DQLSQLBuilder(quickDAOConfig);
        }else if(quickDAOConfig.database instanceof PostgreDatabase){
            dqlsqlBuilder = new PostgreDQLSQLBuilder(quickDAOConfig);
        }else if(quickDAOConfig.database instanceof SQLServerDatabase){
            dqlsqlBuilder = new SQLServerDQLSQLBuilder(quickDAOConfig);
        }else{
            throw new IllegalArgumentException("不支持的数据库类型!");
        }
        return dqlsqlBuilder;
    }
}
