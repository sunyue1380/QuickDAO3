package cn.schoolwow.quickdao.builder.table;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TableBuilderInvocationHandler implements InvocationHandler {
    private AbstractTableBuilder abstractTableBuilder;

    public TableBuilderInvocationHandler(AbstractTableBuilder abstractTableBuilder) {
        this.abstractTableBuilder = abstractTableBuilder;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        abstractTableBuilder.connection = abstractTableBuilder.quickDAOConfig.dataSource.getConnection();
        Object result = method.invoke(abstractTableBuilder,args);
        abstractTableBuilder.connection.close();
        return result;
    }
}
