package cn.schoolwow.quickdao.dao.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;

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
            Connection connection = abstractResponse.query.dqlsqlBuilder.connection;
            //判断是否开启事务
            if (abstractResponse.query.abstractSQLDAO.transaction) {
                //申请数据库连接
                if(null==connection||connection.isClosed()){
                    connection = abstractResponse.query.quickDAOConfig.dataSource.getConnection();
                }
                //设置隔离级别
                if(abstractResponse.query.abstractSQLDAO.transactionIsolation>0){
                    connection.setTransactionIsolation(abstractResponse.query.abstractSQLDAO.transactionIsolation);
                }
                //关闭自动提交功能
                connection.setAutoCommit(false);
            } else {
                connection = abstractResponse.query.quickDAOConfig.dataSource.getConnection();
            }
            abstractResponse.query.dqlsqlBuilder.connection = connection;
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
            if(!abstractResponse.query.abstractSQLDAO.transaction){
                if(null!=abstractResponse.query.dqlsqlBuilder.connection&&!abstractResponse.query.dqlsqlBuilder.connection.isClosed()){
                    abstractResponse.query.dqlsqlBuilder.connection.close();
                }
            }
            MDC.clear();
        }
    }
}
