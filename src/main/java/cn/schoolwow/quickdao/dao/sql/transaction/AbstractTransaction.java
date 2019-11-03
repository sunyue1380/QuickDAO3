package cn.schoolwow.quickdao.dao.sql.transaction;

import cn.schoolwow.quickdao.builder.sql.AbstractSQLBuilder;
import cn.schoolwow.quickdao.dao.sql.dml.AbstractDMLDAO;

import java.sql.SQLException;
import java.sql.Savepoint;

public class AbstractTransaction extends AbstractDMLDAO implements Transaction{

    public AbstractTransaction(AbstractSQLBuilder sqlBuilder) {
        super(sqlBuilder);
    }

    @Override
    public Savepoint setSavePoint(String name) {
        try {
            return sqlBuilder.connection.setSavepoint(name);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void rollback() {
        try {
            sqlBuilder.connection.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void rollback(Savepoint savePoint) {
        try {
            sqlBuilder.connection.rollback(savePoint);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void commit() {
        try {
            sqlBuilder.connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endTransaction() {
        try {
            sqlBuilder.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
