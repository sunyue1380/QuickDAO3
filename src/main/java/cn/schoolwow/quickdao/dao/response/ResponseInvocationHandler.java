package cn.schoolwow.quickdao.dao.response;

import cn.schoolwow.quickdao.builder.sql.dql.AbstractDQLSQLBuilder;
import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ResponseInvocationHandler implements InvocationHandler {
    private Logger logger = LoggerFactory.getLogger(ResponseInvocationHandler.class);
    private AbstractResponse abstractResponse;

    public ResponseInvocationHandler(AbstractResponse abstractResponse) {
        this.abstractResponse = abstractResponse;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            AbstractSQLDAO abstractSQLDAO = abstractResponse.query.abstractSQLDAO;
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
            Object result = method.invoke(abstractResponse, args);
            long endTime = System.currentTimeMillis();
            if(null!=MDC.get("name")){
                logger.debug("[{}]行数:{},耗时:{}ms,执行SQL:{}",MDC.get("name"),MDC.get("count"),(endTime-startTime),MDC.get("sql"));
            }
            return result;
        }catch (InvocationTargetException e){
            if(null!=MDC.get("name")){
                logger.warn("[{}]原始SQL:{}",MDC.get("name"),MDC.get("sql"));
            }
            throw e.getTargetException();
        }finally {
            abstractResponse.query.parameterIndex = 1;
            AbstractDQLSQLBuilder dqlsqlBuilder = abstractResponse.query.dqlsqlBuilder;
            if (!abstractResponse.query.abstractSQLDAO.transaction && !dqlsqlBuilder.connection.isClosed()) {
                dqlsqlBuilder.connection.close();
            }
            MDC.clear();
        }
    }
}
