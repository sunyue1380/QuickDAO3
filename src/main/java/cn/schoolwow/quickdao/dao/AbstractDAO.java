package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.builder.sql.dml.AbstractDMLSQLBuilder;
import cn.schoolwow.quickdao.builder.table.TableBuilder;
import cn.schoolwow.quickdao.dao.condition.*;
import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import cn.schoolwow.quickdao.dao.sql.SQLDAOInvocationHandler;
import cn.schoolwow.quickdao.dao.sql.dml.AbstractDMLDAO;
import cn.schoolwow.quickdao.dao.sql.dml.DMLDAO;
import cn.schoolwow.quickdao.dao.sql.dql.AbstractDQLDAO;
import cn.schoolwow.quickdao.dao.sql.dql.DQLDAO;
import cn.schoolwow.quickdao.dao.sql.transaction.AbstractTransaction;
import cn.schoolwow.quickdao.dao.sql.transaction.Transaction;
import cn.schoolwow.quickdao.database.*;
import cn.schoolwow.quickdao.domain.Query;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class AbstractDAO implements DAO {
    private Logger logger = LoggerFactory.getLogger(DAO.class);
    //数据库建表
    private TableBuilder tableBuilder;
    //数据源配置信息
    private QuickDAOConfig quickDAOConfig;
    //DQL语句构建
    private DMLDAO dmldao;
    //DML语句构建
    private DQLDAO dqldao;
    //数据库操作语言
    private AbstractDMLSQLBuilder dmlsqlBuilder;

    public AbstractDAO(TableBuilder tableBuilder, QuickDAOConfig quickDAOConfig) {
        this.tableBuilder = tableBuilder;
        this.quickDAOConfig = quickDAOConfig;
        {
            this.dqldao = new AbstractDQLDAO(quickDAOConfig.abstractDQLSQLBuilder);
            SQLDAOInvocationHandler invocationHandler = new SQLDAOInvocationHandler((AbstractSQLDAO) this.dqldao);
            this.dqldao = (DQLDAO) Proxy.newProxyInstance(Thread.currentThread()
                    .getContextClassLoader(), new Class<?>[]{DQLDAO.class},invocationHandler);
        }
        {
            this.dmlsqlBuilder = new AbstractDMLSQLBuilder(this.quickDAOConfig);
            this.dmldao = new AbstractDMLDAO(this.dmlsqlBuilder);
            SQLDAOInvocationHandler invocationHandler = new SQLDAOInvocationHandler((AbstractSQLDAO) this.dmldao);
            this.dmldao = (DMLDAO) Proxy.newProxyInstance(Thread.currentThread()
                    .getContextClassLoader(), new Class<?>[]{DMLDAO.class},invocationHandler);
        }
    }

    @Override
    public boolean exist(Object instance) {
        return dqldao.exist(instance);
    }

    @Override
    public <T> T fetch(Class<T> clazz, long id) {
        return dqldao.fetch(clazz,id);
    }

    @Override
    public <T> T fetch(Class<T> clazz, String property, Object value) {
        return dqldao.fetch(clazz,property,value);
    }

    @Override
    public <T> List<T> fetchList(Class<T> clazz, String property, Object value) {
        return dqldao.fetchList(clazz,property,value);
    }

    @Override
    public int insert(Object instance) {
        return dmldao.insert(instance);
    }

    @Override
    public int insert(Object[] instances) {
        return dmldao.insert(instances);
    }

    @Override
    public int insert(Collection instanceCollection) {
        return dmldao.insert(instanceCollection);
    }

    @Override
    public int update(Object instance) {
        return dmldao.update(instance);
    }

    @Override
    public int update(Object[] instances) {
        return dmldao.update(instances);
    }

    @Override
    public int update(Collection instanceCollection) {
        return dmldao.update(instanceCollection);
    }

    @Override
    public int save(Object instance) {
        return dmldao.save(instance);
    }

    @Override
    public int save(Object[] instances) {
        return dmldao.save(instances);
    }

    @Override
    public int save(Collection instanceCollection) {
        return dmldao.save(instanceCollection);
    }

    @Override
    public int delete(Class delete, long id) {
        return dmldao.delete(delete,id);
    }

    @Override
    public int delete(Class clazz, String field, Object value) {
        return dmldao.delete(clazz,field,value);
    }

    @Override
    public int clear(Class clazz) {
        return dmldao.clear(clazz);
    }

    @Override
    public Condition query(Class clazz) {
        Query query = new Query();
        query.entity = quickDAOConfig.entityMap.get(clazz.getName());
        query.quickDAOConfig = this.quickDAOConfig;
        query.dao = this;
        if(quickDAOConfig.database instanceof MySQLDatabase){
            return new MySQLCondition(query);
        }else if(quickDAOConfig.database instanceof H2Database){
            return new H2Condition(query);
        }else if(quickDAOConfig.database instanceof SQLiteDatabase){
            return new SQLiteCondition(query);
        }else if(quickDAOConfig.database instanceof PostgreDatabase){
            return new PostgreCondition(query);
        }else if(quickDAOConfig.database instanceof SQLServerDatabase){
            return new SQLServerCondition(query);
        }else{
            throw new IllegalArgumentException("不支持的数据库类型!");
        }
    }

    @Override
    public Transaction startTransaction() {
        AbstractTransaction transaction = new AbstractTransaction(this.dmlsqlBuilder);
        transaction.transaction = true;
        SQLDAOInvocationHandler sqldaoInvocationHandler = new SQLDAOInvocationHandler(transaction);
        Transaction transactionProxy = (Transaction) Proxy.newProxyInstance(Thread.currentThread()
                .getContextClassLoader(), new Class<?>[]{Transaction.class},sqldaoInvocationHandler);
        return transactionProxy;
    }

    @Override
    public void create(Class clazz) {
        try {
            tableBuilder.createTable(this.quickDAOConfig.entityMap.get(clazz.getName()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drop(Class clazz) {
        try {
            tableBuilder.dropTable(this.quickDAOConfig.entityMap.get(clazz.getName()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void rebuild(Class clazz) {
        try {
            tableBuilder.rebuild(this.quickDAOConfig.entityMap.get(clazz.getName()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
