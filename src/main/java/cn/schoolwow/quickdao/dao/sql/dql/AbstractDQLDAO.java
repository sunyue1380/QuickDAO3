package cn.schoolwow.quickdao.dao.sql.dql;

import cn.schoolwow.quickdao.builder.sql.SQLBuilder;
import cn.schoolwow.quickdao.builder.sql.dql.DQLSQLBuilder;
import cn.schoolwow.quickdao.dao.AbstractDAO;
import cn.schoolwow.quickdao.dao.condition.*;
import cn.schoolwow.quickdao.dao.response.AbstractResponse;
import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import cn.schoolwow.quickdao.database.*;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Query;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import com.alibaba.fastjson.JSONArray;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AbstractDQLDAO extends AbstractSQLDAO implements DQLDAO {
    private DQLSQLBuilder dqlsqlBuilder;

    public AbstractDQLDAO(SQLBuilder sqlBuilder, AbstractDAO abstractDAO) {
        super(sqlBuilder,abstractDAO);
        this.dqlsqlBuilder = (DQLSQLBuilder) sqlBuilder;
    }

    @Override
    public <T> T fetch(Class<T> clazz, long id) {
        return fetch(clazz,abstractDAO.quickDAOConfig.entityMap.get(clazz.getName()).id.column,id);
    }

    @Override
    public <T> T fetch(Class<T> clazz, String field, Object value) {
        List<T> list = fetchList(clazz,field,value);
        if(null==list||list.isEmpty()){
            return null;
        }
        return list.get(0);
    }

    @Override
    public <T> List<T> fetchList(Class<T> clazz, String field, Object value) {
        try {
            PreparedStatement ps = null;
            if(null==value){
                ps = dqlsqlBuilder.fetchNull(clazz,field);
            }else{
                ps = dqlsqlBuilder.fetch(clazz,field,value);
            }
            Entity entity = abstractDAO.quickDAOConfig.entityMap.get(clazz.getName());
            ResultSet resultSet = ps.executeQuery();
            JSONArray array = new JSONArray();
            while(resultSet.next()){
                array.add(AbstractResponse.getObject(entity, AbstractCondition.mainTableAlias,resultSet));
            }
            resultSet.close();
            ps.close();
            return array.toJavaList(clazz);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public Condition query(Class clazz) {
        Query query = new Query();
        query.entity = abstractDAO.quickDAOConfig.entityMap.get(clazz.getName());
        query.quickDAOConfig = abstractDAO.quickDAOConfig;
        query.dqlsqlBuilder = this.dqlsqlBuilder;
        query.dao = abstractDAO;
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
