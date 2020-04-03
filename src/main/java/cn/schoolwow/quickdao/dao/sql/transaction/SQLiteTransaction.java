package cn.schoolwow.quickdao.dao.sql.transaction;

import cn.schoolwow.quickdao.builder.sql.SQLBuilder;
import cn.schoolwow.quickdao.dao.AbstractDAO;
import cn.schoolwow.quickdao.dao.SQLiteDAO;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;

import java.sql.SQLException;

public class SQLiteTransaction extends AbstractTransaction{
    private SQLiteDAO sqLiteDAO;

    public SQLiteTransaction(SQLBuilder sqlBuilder, AbstractDAO abstractDAO) {
        super(sqlBuilder, abstractDAO);
        sqLiteDAO = (SQLiteDAO) abstractDAO;
    }

    @Override
    public void endTransaction() {
        try {
            sqlBuilder.connection.close();
            sqLiteDAO.quickDAOConfig.reentrantLock.unlock();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }
}
