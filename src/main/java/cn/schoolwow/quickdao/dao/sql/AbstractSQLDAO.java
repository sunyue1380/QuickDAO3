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
            Entity entity = abstractDAO.quickDAOConfig.entityMap.get(instance.getClass().getName());
            PreparedStatement ps = null;
            if(entity.uniqueKeyProperties.length>0){
                ps = sqlBuilder.selectByUniqueKey(instance);
            }else if(null!=entity.id){
                ps = sqlBuilder.selectById(instance);
            }
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getLong(1)>0;
            }
            resultSet.close();
            ps.close();
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        return result;
    }
}
