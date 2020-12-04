package cn.schoolwow.quickdao.dao.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**DAO接口调用方法*/
public class SQLDAOInvocationHandler implements InvocationHandler {
    private Logger logger = LoggerFactory.getLogger(SQLDAOInvocationHandler.class);
    private AbstractSQLDAO abstractSQLDAO;

    public SQLDAOInvocationHandler(AbstractSQLDAO abstractSQLDAO) {
        this.abstractSQLDAO = abstractSQLDAO;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = null;
        try {
            if(null!=abstractSQLDAO.sqlBuilder.quickDAOConfig.reentrantLock){
                abstractSQLDAO.sqlBuilder.quickDAOConfig.reentrantLock.lock();
            }
            //调用query方法不会申请数据库连接
            if(!"query".equals(method.getName())){
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
            }
            long startTime = System.currentTimeMillis();
            result = method.invoke(abstractSQLDAO, args);
            long endTime = System.currentTimeMillis();
            if(null!=MDC.get("name")){
                logger.debug("[{}]行数:{},耗时:{}ms,执行SQL:{}",MDC.get("name"),MDC.get("count"),endTime-startTime,MDC.get("sql"));
            }
            return result;
        }catch (InvocationTargetException e){
            if(null!=MDC.get("name")){
                logger.debug("[{}]原始SQL:{}",MDC.get("name"),MDC.get("sql"));
            }
            throw e.getTargetException();
        }finally {
            if(null!=abstractSQLDAO.sqlBuilder.quickDAOConfig.reentrantLock){
                abstractSQLDAO.sqlBuilder.quickDAOConfig.reentrantLock.unlock();
            }
            if (!abstractSQLDAO.transaction&&null!=abstractSQLDAO.sqlBuilder.connection&&!abstractSQLDAO.sqlBuilder.connection.isClosed()) {
                abstractSQLDAO.sqlBuilder.connection.close();
            }
        }
    }
}
