package cn.schoolwow.quickdao.dao.sql.dql;

import cn.schoolwow.quickdao.builder.sql.AbstractSQLBuilder;
import cn.schoolwow.quickdao.builder.sql.dql.DQLSQLBuilder;
import cn.schoolwow.quickdao.dao.condition.AbstractCondition;
import cn.schoolwow.quickdao.dao.response.AbstractResponse;
import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import cn.schoolwow.quickdao.domain.Entity;
import com.alibaba.fastjson.JSONArray;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AbstractDQLDAO extends AbstractSQLDAO implements DQLDAO {
    private DQLSQLBuilder dqlsqlBuilder;

    public AbstractDQLDAO(AbstractSQLBuilder sqlBuilder) {
        super(sqlBuilder);
        this.dqlsqlBuilder = (DQLSQLBuilder) sqlBuilder;
    }

    @Override
    public <T> T fetch(Class<T> clazz, long id) {
        return fetch(clazz,sqlBuilder.quickDAOConfig.entityMap.get(clazz.getName()).id.column,id);
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
            Entity entity = sqlBuilder.quickDAOConfig.entityMap.get(clazz.getName());
            ResultSet resultSet = ps.executeQuery();
            JSONArray array = new JSONArray();
            while(resultSet.next()){
                array.add(AbstractResponse.getObject(entity, AbstractCondition.mainTableAlias,resultSet));
            }
            resultSet.close();
            ps.close();
            return array.toJavaList(clazz);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
