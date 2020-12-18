package cn.schoolwow.quickdao.dao.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;

/**DAO接口调用方法*/
public class SQLDAOInvocationHandler implements InvocationHandler {
    private Logger logger = LoggerFactory.getLogger(SQLDAOInvocationHandler.class);
    private AbstractSQLDAO abstractSQLDAO;

    public SQLDAOInvocationHandler(AbstractSQLDAO abstractSQLDAO) {
        this.abstractSQLDAO = abstractSQLDAO;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //调用结束事务方法直接返回
        if("endTransaction".equals(method.getName())){
            return method.invoke(abstractSQLDAO, args);
        }
        //未开启事务的情况下调用query方法则直接调用返回
        if("query".equals(method.getName())&&!abstractSQLDAO.transaction){
            return method.invoke(abstractSQLDAO, args);
        }
        try {
            Connection connection = abstractSQLDAO.sqlBuilder.connection;
            //判断是否开启事务
            if (abstractSQLDAO.transaction) {
                //申请数据库连接
                if(null==connection||connection.isClosed()){
                    connection = abstractSQLDAO.quickDAOConfig.dataSource.getConnection();
                }
                //设置隔离级别
                if(abstractSQLDAO.transactionIsolation>0){
                    connection.setTransactionIsolation(abstractSQLDAO.transactionIsolation);
                }
                //关闭自动提交功能
                connection.setAutoCommit(false);
            } else {
                connection = abstractSQLDAO.quickDAOConfig.dataSource.getConnection();
            }
            abstractSQLDAO.sqlBuilder.connection = connection;

            long startTime = System.currentTimeMillis();
            Object result = method.invoke(abstractSQLDAO, args);
            long endTime = System.currentTimeMillis();
            if(null!=MDC.get("name")){
                if(null==MDC.get("count")){
                    logger.debug("[{}]耗时:{}ms,执行SQL:{}",MDC.get("name"),endTime-startTime,MDC.get("sql"));
                }else{
                    logger.debug("[{}]行数:{},耗时:{}ms,执行SQL:{}",MDC.get("name"),MDC.get("count"),endTime-startTime,MDC.get("sql"));
                }
            }
            return result;
        }catch (InvocationTargetException e){
            if(null!=MDC.get("name")){
                logger.warn("[{}]原始SQL:{}",MDC.get("name"),MDC.get("sql"));
            }
            throw e.getTargetException();
        }finally {
            if(!abstractSQLDAO.transaction){
                if(null!=abstractSQLDAO.sqlBuilder.connection&&!abstractSQLDAO.sqlBuilder.connection.isClosed()){
                    abstractSQLDAO.sqlBuilder.connection.close();
                }
            }
            MDC.clear();
        }
    }
}
