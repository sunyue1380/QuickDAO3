package cn.schoolwow.quickdao.dao.sql.transaction;

import cn.schoolwow.quickdao.dao.sql.dml.AbstractDMLDAO;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;

import java.sql.SQLException;
import java.sql.Savepoint;

public class AbstractTransaction extends AbstractDMLDAO implements Transaction{

    public AbstractTransaction(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public void setTransactionIsolation(int transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
    }

    @Override
    public Savepoint setSavePoint(String name) {
        try {
            return sqlBuilder.connection.setSavepoint(name);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void rollback() {
        try {
            sqlBuilder.connection.rollback();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void rollback(Savepoint savePoint) {
        try {
            sqlBuilder.connection.rollback(savePoint);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void commit() {
        try {
            sqlBuilder.connection.commit();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void endTransaction() {
        transaction = false;
        try {
            sqlBuilder.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
