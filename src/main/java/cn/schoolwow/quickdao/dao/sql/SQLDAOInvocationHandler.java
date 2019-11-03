package cn.schoolwow.quickdao.dao.sql;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;

public class SQLDAOInvocationHandler implements InvocationHandler {
    private AbstractSQLDAO abstractSQLDAO;
    //保存开启事务的数据库连接
    private Connection connection;

    public SQLDAOInvocationHandler(AbstractSQLDAO abstractSQLDAO) {
        this.abstractSQLDAO = abstractSQLDAO;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(abstractSQLDAO.transaction){
            if(null==connection||connection.isClosed()){
                this.connection = abstractSQLDAO.sqlBuilder.quickDAOConfig.dataSource.getConnection();
                this.connection.setAutoCommit(false);
            }
            abstractSQLDAO.sqlBuilder.connection = this.connection;
        }else{
            abstractSQLDAO.sqlBuilder.connection = abstractSQLDAO.sqlBuilder.quickDAOConfig.dataSource.getConnection();
        }
        Object result = method.invoke(abstractSQLDAO,args);
        if(!abstractSQLDAO.transaction&&!abstractSQLDAO.sqlBuilder.connection.isClosed()){
            abstractSQLDAO.sqlBuilder.connection.close();
        }
        return result;
    }
}
