package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.builder.sql.dml.AbstractDMLSQLBuilder;
import cn.schoolwow.quickdao.builder.sql.dql.*;
import cn.schoolwow.quickdao.builder.table.TableBuilder;
import cn.schoolwow.quickdao.dao.condition.Condition;
import cn.schoolwow.quickdao.dao.sql.SQLDAOInvocationHandler;
import cn.schoolwow.quickdao.dao.sql.dml.AbstractDMLDAO;
import cn.schoolwow.quickdao.dao.sql.dml.DMLDAO;
import cn.schoolwow.quickdao.dao.sql.dql.AbstractDQLDAO;
import cn.schoolwow.quickdao.dao.sql.dql.DQLDAO;
import cn.schoolwow.quickdao.dao.sql.transaction.AbstractTransaction;
import cn.schoolwow.quickdao.dao.sql.transaction.Transaction;
import cn.schoolwow.quickdao.database.*;
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
    public QuickDAOConfig quickDAOConfig;

    public AbstractDAO(TableBuilder tableBuilder, QuickDAOConfig quickDAOConfig) {
        this.tableBuilder = tableBuilder;
        this.quickDAOConfig = quickDAOConfig;
    }

    @Override
    public boolean exist(Object instance) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.exist(instance);
    }

    @Override
    public <T> T fetch(Class<T> clazz, long id) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.fetch(clazz,id);
    }

    @Override
    public <T> T fetch(Class<T> clazz, String property, Object value) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.fetch(clazz,property,value);
    }

    @Override
    public <T> List<T> fetchList(Class<T> clazz, String property, Object value) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.fetchList(clazz,property,value);
    }

    @Override
    public Condition query(Class clazz) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.query(clazz);
    }

    @Override
    public int insert(Object instance) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.insert(instance);
    }

    @Override
    public int insert(Object[] instances) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.insert(instances);
    }

    @Override
    public int insert(Collection instanceCollection) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.insert(instanceCollection);
    }

    @Override
    public int update(Object instance) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.update(instance);
    }

    @Override
    public int update(Object[] instances) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.update(instances);
    }

    @Override
    public int update(Collection instanceCollection) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.update(instanceCollection);
    }

    @Override
    public int save(Object instance) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.save(instance);
    }

    @Override
    public int save(Object[] instances) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.save(instances);
    }

    @Override
    public int save(Collection instanceCollection) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.save(instanceCollection);
    }

    @Override
    public int delete(Class delete, long id) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.delete(delete,id);
    }

    @Override
    public int delete(Class clazz, String field, Object value) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.delete(clazz,field,value);
    }

    @Override
    public int clear(Class clazz) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.clear(clazz);
    }

    @Override
    public Transaction startTransaction() {
        AbstractDMLSQLBuilder dmlsqlBuilder = new AbstractDMLSQLBuilder(quickDAOConfig);
        AbstractTransaction transaction = new AbstractTransaction(dmlsqlBuilder,this);
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

    /**创建DMLDAO*/
    private DMLDAO createDMLDAO(){
        AbstractDMLSQLBuilder dmlsqlBuilder = new AbstractDMLSQLBuilder(quickDAOConfig);
        SQLDAOInvocationHandler sqldaoInvocationHandler = new SQLDAOInvocationHandler(new AbstractDMLDAO(dmlsqlBuilder,this));
        DMLDAO dmldao = (DMLDAO) Proxy.newProxyInstance(Thread.currentThread()
                .getContextClassLoader(), new Class<?>[]{DMLDAO.class},sqldaoInvocationHandler);
        return dmldao;
    }

    /**创建DQLDAO*/
    private DQLDAO createDQLDAO(){
        AbstractDQLSQLBuilder dqlsqlBuilder = null;
        if(quickDAOConfig.database instanceof MySQLDatabase){
            dqlsqlBuilder = new MySQLDQLSQLBuilder(quickDAOConfig);
        }else if(quickDAOConfig.database instanceof SQLiteDatabase){
            dqlsqlBuilder = new SQLiteDQLSQLBuilder(quickDAOConfig);
        }else if(quickDAOConfig.database instanceof H2Database){
            dqlsqlBuilder = new H2DQLSQLBuilder(quickDAOConfig);
        }else if(quickDAOConfig.database instanceof PostgreDatabase){
            dqlsqlBuilder = new PostgreDQLSQLBuilder(quickDAOConfig);
        }else if(quickDAOConfig.database instanceof SQLServerDatabase){
            dqlsqlBuilder = new SQLServerDQLSQLBuilder(quickDAOConfig);
        }else{
            throw new IllegalArgumentException("不支持的数据库类型!");
        }
        SQLDAOInvocationHandler sqldaoInvocationHandler = new SQLDAOInvocationHandler(new AbstractDQLDAO(dqlsqlBuilder,this));
        DQLDAO dqldao = (DQLDAO) Proxy.newProxyInstance(Thread.currentThread()
                .getContextClassLoader(), new Class<?>[]{DQLDAO.class},sqldaoInvocationHandler);
        return dqldao;
    }
}
