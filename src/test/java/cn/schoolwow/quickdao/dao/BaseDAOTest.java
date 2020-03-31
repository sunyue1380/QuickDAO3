package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.QuickDAO;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;

public class BaseDAOTest {
    protected DAO dao;

    @Parameterized.Parameters
    public static Collection prepareData() {
        BasicDataSource mysqlDataSource = new BasicDataSource();
        mysqlDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        mysqlDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/quickdao");
        mysqlDataSource.setUsername("root");
        mysqlDataSource.setPassword("123456");

        BasicDataSource sqliteDataSource = new BasicDataSource();
        sqliteDataSource.setDriverClassName("org.sqlite.JDBC");
        sqliteDataSource.setUrl("jdbc:sqlite:" + new File("quickdao_sqlite.db").getAbsolutePath());

        BasicDataSource h2DataSource = new BasicDataSource();
        h2DataSource.setDriverClassName("org.h2.Driver");
        h2DataSource.setUrl("jdbc:h2:" + new File("quickdao_h2.db").getAbsolutePath() + ";mode=MYSQL");

        BasicDataSource postgreDataSource = new BasicDataSource();
        postgreDataSource.setDriverClassName("org.postgresql.Driver");
        postgreDataSource.setUrl("jdbc:postgresql://127.0.0.1:5432/quickdao");
        postgreDataSource.setUsername("postgres");
        postgreDataSource.setPassword("postgres");

        BasicDataSource sqlServerDataSource = new BasicDataSource();
        sqlServerDataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        sqlServerDataSource.setUrl("jdbc:sqlserver://127.0.0.1:1433;databaseName=quickdao");
        sqlServerDataSource.setUsername("sa");
        sqlServerDataSource.setPassword("aa1122335");

        //各种数据库产品
        DataSource[] dataSources = {mysqlDataSource, sqliteDataSource, h2DataSource, postgreDataSource,sqliteDataSource};
        Object[][] data = new Object[dataSources.length][1];
        for (int i = 0; i < dataSources.length; i++) {
            DAO dao = QuickDAO.newInstance().dataSource(dataSources[i])
                    .packageName("cn.schoolwow.quickdao.entity")
                    .autoCreateTable(true)
                    .build();
            data[i][0] = dao;
        }
        return Arrays.asList(data);
    }

    public BaseDAOTest(DAO dao) {
        this.dao = dao;
    }
}
