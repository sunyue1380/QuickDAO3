package cn.schoolwow.quickdao.dao.sql;

import cn.schoolwow.quickdao.builder.sql.AbstractSQLBuilder;
import cn.schoolwow.quickdao.builder.sql.SQLBuilder;
import cn.schoolwow.quickdao.dao.AbstractDAO;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
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

    public AbstractSQLDAO(SQLBuilder sqlBuilder, AbstractDAO abstractDAO) {
        this.sqlBuilder = (AbstractSQLBuilder) sqlBuilder;
        this.abstractDAO = abstractDAO;
    }

    @Override
    public boolean exist(Object instance) {
        if(null==instance){
            return false;
        }
        boolean result = false;
        try {
            if(hasId(instance)){
                result = true;
            }else{
                PreparedStatement ps = sqlBuilder.selectByUniqueKey(instance);
                ResultSet resultSet = ps.executeQuery();
                if (resultSet.next()) {
                    result = resultSet.getLong(1)>0;
                }
                resultSet.close();
                ps.close();
            }
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        return result;
    }

    /**
     * 对象是否存在id
     * 默认主键必须存在且为long型
     */
    public boolean hasId(Object instance) throws Exception {
        Entity entity = abstractDAO.quickDAOConfig.entityMap.get(instance.getClass().getName());
        if(null==entity.id){
            return false;
        }
        Field field = instance.getClass().getDeclaredField(entity.id.name);
        field.setAccessible(true);
        switch(field.getType().getSimpleName().toLowerCase()){
            case "long":{
                return field.getLong(instance)>0;
            }
            case "string":{
                return field.get(instance)!=null;
            }
        }
        throw new IllegalArgumentException("不支持的主键类型!当前主键类型:"+field.getType().getName());
    }
}
