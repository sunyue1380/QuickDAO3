package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.builder.table.TableBuilder;
import cn.schoolwow.quickdao.dao.sql.SQLDAOInvocationHandler;
import cn.schoolwow.quickdao.dao.sql.transaction.AbstractTransaction;
import cn.schoolwow.quickdao.dao.sql.transaction.SQLiteTransaction;
import cn.schoolwow.quickdao.dao.sql.transaction.Transaction;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.locks.ReentrantLock;

public class SQLiteDAO extends AbstractDAO {
    private Logger logger = LoggerFactory.getLogger(SQLiteDAO.class);

    public SQLiteDAO(TableBuilder tableBuilder, QuickDAOConfig quickDAOConfig) {
        super(tableBuilder, quickDAOConfig);
        quickDAOConfig.reentrantLock = new ReentrantLock();
    }

    @Override
    public Transaction startTransaction() {
        quickDAOConfig.reentrantLock.lock();
        AbstractTransaction transaction = new SQLiteTransaction(this);
        transaction.transaction = true;
        InvocationHandler sqldaoInvocationHandler = new SQLDAOInvocationHandler(transaction);
        Transaction transactionProxy = (Transaction) Proxy.newProxyInstance(Thread.currentThread()
                .getContextClassLoader(), new Class<?>[]{Transaction.class},sqldaoInvocationHandler);
        return transactionProxy;
    }
}
