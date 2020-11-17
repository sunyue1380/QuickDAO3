package cn.schoolwow.quickdao.dao.sql.dql;

import cn.schoolwow.quickdao.builder.sql.dql.AbstractDQLSQLBuilder;
import cn.schoolwow.quickdao.dao.AbstractDAO;
import cn.schoolwow.quickdao.dao.response.AbstractResponse;
import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.MDC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AbstractDQLDAO extends AbstractSQLDAO implements DQLDAO {
    private AbstractDQLSQLBuilder dqlsqlBuilder;

    public AbstractDQLDAO(AbstractDAO abstractDAO) {
        super(abstractDAO);
        dqlsqlBuilder = (AbstractDQLSQLBuilder) sqlBuilder;
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
                array.add(AbstractResponse.getObject(entity, "t",resultSet));
            }
            resultSet.close();
            ps.close();
            MDC.put("count",array.size()+"");
            return array.toJavaList(clazz);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public JSONObject fetch(String tableName, String field, Object value) {
        JSONArray array = fetchList(tableName,field,value);
        if(null==array||array.isEmpty()){
            return null;
        }
        return array.getJSONObject(0);
    }

    @Override
    public JSONArray fetchList(String tableName, String field, Object value) {
        try {
            PreparedStatement ps = null;
            if(null==value){
                ps = dqlsqlBuilder.fetchNull(tableName,field);
            }else{
                ps = dqlsqlBuilder.fetch(tableName,field,value);
            }
            Entity dbEntity = abstractDAO.quickDAOConfig.getDbEntityByTableName(tableName);
            ResultSet resultSet = ps.executeQuery();
            JSONArray array = new JSONArray();
            while(resultSet.next()){
                array.add(AbstractResponse.getObject(dbEntity, "t",resultSet));
            }
            resultSet.close();
            ps.close();
            MDC.put("count",array.size()+"");
            return array;
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }
}
