package cn.schoolwow.quickdao.dao.response;

import cn.schoolwow.quickdao.builder.sql.AbstractSQLBuilder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ResponseInvocationHandler implements InvocationHandler {
    private AbstractResponse abstractResponse;

    public ResponseInvocationHandler(AbstractResponse abstractResponse) {
        this.abstractResponse = abstractResponse;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        abstractResponse.connection = abstractResponse.query.quickDAOConfig.dataSource.getConnection();
        AbstractSQLBuilder abstractSQLBuilder = (AbstractSQLBuilder) abstractResponse.query.dqlsqlBuilder;
        abstractSQLBuilder.connection = abstractResponse.connection;
        Object result = method.invoke(abstractResponse,args);
        abstractResponse.query.parameterIndex = 1;
        abstractResponse.connection.close();
        return result;
    }
}
