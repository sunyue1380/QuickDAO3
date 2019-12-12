package cn.schoolwow.quickdao.dao.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SQLDAOInvocationHandler implements InvocationHandler {
    private Logger logger = LoggerFactory.getLogger(SQLDAOInvocationHandler.class);
    private AbstractSQLDAO abstractSQLDAO;

    public SQLDAOInvocationHandler(AbstractSQLDAO abstractSQLDAO) {
        this.abstractSQLDAO = abstractSQLDAO;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(method.getDeclaringClass().getName().equals(Object.class.getName())){
            return method.invoke(abstractSQLDAO,args);
        }
        if(abstractSQLDAO.transaction){
            if(null==abstractSQLDAO.sqlBuilder.connection||abstractSQLDAO.sqlBuilder.connection.isClosed()){
                abstractSQLDAO.sqlBuilder.connection = abstractSQLDAO.sqlBuilder.quickDAOConfig.dataSource.getConnection();
                abstractSQLDAO.sqlBuilder.connection.setAutoCommit(false);
            }
        }else{
            abstractSQLDAO.sqlBuilder.connection = abstractSQLDAO.sqlBuilder.quickDAOConfig.dataSource.getConnection();
        }
        try {
            Object result = method.invoke(abstractSQLDAO,args);
            if(!abstractSQLDAO.transaction&&!abstractSQLDAO.sqlBuilder.connection.isClosed()){
                abstractSQLDAO.sqlBuilder.connection.close();
            }
            return result;
        } catch (InvocationTargetException e){
            throw e.getTargetException();
        }
    }
}
