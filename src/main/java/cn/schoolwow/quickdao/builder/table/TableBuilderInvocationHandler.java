package cn.schoolwow.quickdao.builder.table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TableBuilderInvocationHandler implements InvocationHandler {
    private static Logger logger = LoggerFactory.getLogger(TableBuilderInvocationHandler.class);
    private AbstractTableBuilder abstractTableBuilder;

    public TableBuilderInvocationHandler(AbstractTableBuilder abstractTableBuilder) {
        this.abstractTableBuilder = abstractTableBuilder;
    }

    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            abstractTableBuilder.connection = abstractTableBuilder.quickDAOConfig.dataSource.getConnection();
            Object result = method.invoke(abstractTableBuilder,args);
            if(!"refreshDbEntityList".equals(method.getName())){
                abstractTableBuilder.refreshDbEntityList();
            }
            return result;
        }catch (InvocationTargetException e){
            throw e.getTargetException();
        }finally {
            abstractTableBuilder.connection.close();
        }
    }
}
