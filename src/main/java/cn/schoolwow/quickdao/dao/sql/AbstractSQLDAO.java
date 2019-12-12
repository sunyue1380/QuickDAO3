package cn.schoolwow.quickdao.dao.sql;

import cn.schoolwow.quickdao.builder.sql.AbstractSQLBuilder;
import cn.schoolwow.quickdao.builder.sql.SQLBuilder;
import cn.schoolwow.quickdao.dao.AbstractDAO;
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
        Field field = instance.getClass().getDeclaredField(abstractDAO.quickDAOConfig.entityMap.get(instance.getClass().getName()).id.name);
        field.setAccessible(true);
        return field.getLong(instance) > 0;
    }
}
