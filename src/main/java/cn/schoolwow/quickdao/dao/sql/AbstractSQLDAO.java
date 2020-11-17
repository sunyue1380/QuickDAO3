package cn.schoolwow.quickdao.dao.sql;

import cn.schoolwow.quickdao.builder.sql.AbstractSQLBuilder;
import cn.schoolwow.quickdao.builder.sql.SQLBuilder;
import cn.schoolwow.quickdao.builder.sql.dql.AbstractDQLSQLBuilder;
import cn.schoolwow.quickdao.builder.sql.dql.DQLSQLBuilder;
import cn.schoolwow.quickdao.dao.AbstractDAO;
import cn.schoolwow.quickdao.dao.condition.*;
import cn.schoolwow.quickdao.database.*;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.Query;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AbstractSQLDAO implements SQLDAO {
    protected Logger logger = LoggerFactory.getLogger(SQLDAO.class);
    //SQL语句构建
    public AbstractSQLBuilder sqlBuilder;
    //DAO对象
    public AbstractDAO abstractDAO;
    //是否开启事务
    public boolean transaction = false;
    //事务隔离级别
    public int transactionIsolation;

    public AbstractSQLDAO(AbstractDAO abstractDAO) {
        this.sqlBuilder = SQLBuilder.getDQLSQLBuilderInstance(abstractDAO.quickDAOConfig);
        this.abstractDAO = abstractDAO;
    }

    @Override
    public boolean exist(Object instance) {
        if(null==instance){
            return false;
        }
        boolean result = false;
        try {
            Entity entity = abstractDAO.quickDAOConfig.entityMap.get(instance.getClass().getName());
            PreparedStatement ps = null;
            if(null!=entity.uniqueKeyProperties&&entity.uniqueKeyProperties.length>0){
                ps = sqlBuilder.selectCountByUniqueKey(instance);
            }else if(null!=entity.id){
                ps = sqlBuilder.selectCountById(instance);
            }else{
                throw new IllegalArgumentException("该实例无唯一性约束又无id值,无法判断!类名:"+instance.getClass().getName());
            }
            MDC.put("count","0");
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getLong(1)>0;
                MDC.put("count","1");
            }
            resultSet.close();
            ps.close();
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        return result;
    }

    @Override
    public Condition query(Class clazz) {
        Entity entity = abstractDAO.quickDAOConfig.entityMap.get(clazz.getName());
        if(null==entity){
            throw new IllegalArgumentException("不存在的实体类:"+clazz.getName()+"!");
        }
        return query(entity);
    }

    @Override
    public Condition query(String tableName) {
        for(Entity entity:abstractDAO.quickDAOConfig.dbEntityList){
            if(entity.tableName.equals(tableName)){
                return query(entity);
            }
        }
        for(Entity entity:abstractDAO.quickDAOConfig.visualTableList){
            if(entity.tableName.equals(tableName)){
                return query(entity);
            }
        }
        throw new IllegalArgumentException("不存在的表名:"+tableName+"!");
    }

    @Override
    public Condition query(Condition condition) {
        condition.execute();
        Query fromQuery = ((AbstractCondition) condition).query;

        Entity entity = new Entity();
        entity.clazz = JSONObject.class;
        AbstractDQLSQLBuilder dqlsqlBuilder = null;
        if(sqlBuilder instanceof DQLSQLBuilder){
            dqlsqlBuilder = (AbstractDQLSQLBuilder) sqlBuilder;
        }else{
            dqlsqlBuilder = SQLBuilder.getDQLSQLBuilderInstance(fromQuery.quickDAOConfig);
        }
        entity.tableName = "( " + dqlsqlBuilder.getArraySQL(fromQuery).toString() +" )";
        entity.escapeTableName = entity.tableName;
        entity.properties = new Property[0];
        AbstractCondition condition1 = (AbstractCondition) query(entity);
        condition1.query.fromQuery = fromQuery;
        return condition1;
    }

    private Condition query(Entity entity){
        Query query = new Query();
        query.entity = entity;
        query.quickDAOConfig = abstractDAO.quickDAOConfig;
        AbstractDQLSQLBuilder dqlsqlBuilder = null;
        if(sqlBuilder instanceof DQLSQLBuilder){
            dqlsqlBuilder = (AbstractDQLSQLBuilder) sqlBuilder;
        }else{
            dqlsqlBuilder = SQLBuilder.getDQLSQLBuilderInstance(query.quickDAOConfig);
            dqlsqlBuilder.connection = sqlBuilder.connection;
        }
        query.dqlsqlBuilder = dqlsqlBuilder;
        query.dao = abstractDAO;
        query.abstractSQLDAO = this;
        if(query.quickDAOConfig.database instanceof MySQLDatabase){
            return new MySQLCondition(query);
        }else if(query.quickDAOConfig.database instanceof H2Database){
            return new H2Condition(query);
        }else if(query.quickDAOConfig.database instanceof SQLiteDatabase){
            return new SQLiteCondition(query);
        }else if(query.quickDAOConfig.database instanceof PostgreDatabase){
            return new PostgreCondition(query);
        }else if(query.quickDAOConfig.database instanceof SQLServerDatabase){
            return new SQLServerCondition(query);
        }else{
            throw new IllegalArgumentException("不支持的数据库类型!");
        }
    }
}
