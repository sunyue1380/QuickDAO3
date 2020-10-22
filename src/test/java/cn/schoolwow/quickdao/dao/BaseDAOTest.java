package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.QuickDAO;
import cn.schoolwow.quickdao.dao.sql.transaction.Transaction;
import cn.schoolwow.quickdao.entity.Product;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.runners.Parameterized;

import javax.sql.DataSource;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

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
        postgreDataSource.setPassword("123456");

        BasicDataSource sqlServerDataSource = new BasicDataSource();
        sqlServerDataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        sqlServerDataSource.setUrl("jdbc:sqlserver://127.0.0.1:1433;databaseName=quickdao");
        sqlServerDataSource.setUsername("sa");
        sqlServerDataSource.setPassword("aa1122335");

        //各种数据库产品
//        DataSource[] dataSources = {mysqlDataSource, sqliteDataSource, h2DataSource, postgreDataSource};
        DataSource[] dataSources = {mysqlDataSource};
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
        initialize();
    }

    /**初始化数据*/
    private void initialize(){
        dao.rebuild(Product.class);
        Transaction transaction = dao.startTransaction();
        String[] productNames = new String[]{"笔记本电脑","冰箱","电视机","智能音箱"};
        String[] types = new String[]{"电器","电器","电器","数码"};
        int[] prices = new int[]{4000,600,3000,1000};
        for(int i=0;i<productNames.length;i++){
            Product product = new Product();
            product.setName(productNames[i]);
            product.setType(types[i]);
            product.setPrice(prices[i]);
            product.setPublishTime(new Date());
            product.setPersonId(1);
            transaction.insert(product);
        }
        transaction.commit();
        transaction.endTransaction();
    }
}
