package cn.schoolwow.quickdao.dao.sql;

import cn.schoolwow.quickdao.dao.sql.dml.DMLDAO;
import cn.schoolwow.quickdao.dao.sql.dql.DQLDAO;
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
        if(!method.getDeclaringClass().getName().equals(DQLDAO.class.getName())
                &&! method.getDeclaringClass().getName().equals(DMLDAO.class.getName())){
            Object result = method.invoke(abstractSQLDAO, args);
            return result;
        }
        try {
            if(null!=abstractSQLDAO.sqlBuilder.quickDAOConfig.reentrantLock){
                abstractSQLDAO.sqlBuilder.quickDAOConfig.reentrantLock.lock();
            }
            //判断是否开启事务
            if (abstractSQLDAO.transaction) {
                if (null == abstractSQLDAO.sqlBuilder.connection || abstractSQLDAO.sqlBuilder.connection.isClosed()) {
                    abstractSQLDAO.sqlBuilder.connection = abstractSQLDAO.sqlBuilder.quickDAOConfig.dataSource.getConnection();
                    if (abstractSQLDAO.transactionIsolation > 0) {
                        abstractSQLDAO.sqlBuilder.connection.setTransactionIsolation(abstractSQLDAO.transactionIsolation);
                    }
                    abstractSQLDAO.sqlBuilder.connection.setAutoCommit(false);
                }
            } else {
                abstractSQLDAO.sqlBuilder.connection = abstractSQLDAO.sqlBuilder.quickDAOConfig.dataSource.getConnection();
            }
            Object result = method.invoke(abstractSQLDAO, args);
            if (!abstractSQLDAO.transaction && !abstractSQLDAO.sqlBuilder.connection.isClosed()) {
                abstractSQLDAO.sqlBuilder.connection.close();
            }
            return result;
        }catch (InvocationTargetException e){
            throw e.getTargetException();
        }finally {
            if(null!=abstractSQLDAO.sqlBuilder.quickDAOConfig.reentrantLock){
                abstractSQLDAO.sqlBuilder.quickDAOConfig.reentrantLock.unlock();
            }
        }
    }
}
