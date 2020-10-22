package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.QuickDAO;
import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.entity.DownloadTask;
import cn.schoolwow.quickdao.entity.Order;
import cn.schoolwow.quickdao.entity.Person;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;

/**配置项测试*/
public class ConfigTest{
    @Test
    public void testConfig(){
        DataSource[] dataSources = getDataSources();
        for(DataSource dataSource:dataSources){
            scan(dataSource);
            define(dataSource);
        }
    }

    private void scan(DataSource dataSource){
        DAO dao = QuickDAO.newInstance()
                .dataSource(dataSource)
                .entity(Person.class)
                .entity(Order.class)
                .build();
        AbstractDAO abstractDAO = (AbstractDAO)dao;
        QuickDAOConfig quickDAOConfig = abstractDAO.quickDAOConfig;
        Assert.assertEquals(2,quickDAOConfig.entityMap.size());
        Entity entity = quickDAOConfig.entityMap.get(Person.class.getName());
        Assert.assertEquals("person",entity.tableName);
        Assert.assertEquals(quickDAOConfig.database.escape(entity.tableName),entity.escapeTableName);

        //正常执行操作
        dao.rebuild(Person.class);
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("Bill");
            person.setLastName("Gates");
            person.setAddress("Xuanwumen 10");
            person.setCity("Beijing");
            int effect = dao.insert(person);
            Assert.assertEquals(1, effect);
        }
        {
            Person person = dao.fetch(Person.class,1);
            Assert.assertEquals("Gates", person.getLastName());
        }
        {
            Person person = (Person) dao.query(Person.class)
                    .addQuery("lastName","Gates")
                    .execute()
                    .getOne();
            Assert.assertNotNull(person);
        }
    }

    private void define(DataSource dataSource){
        DAO dao = QuickDAO.newInstance()
                .dataSource(dataSource)
                .entity(DownloadTask.class)
                .define(DownloadTask.class)
                .tableName("download_task")
                .comment("下载任务类")
                .property("filePath")
                .id(true)
                .comment("文件路径(主键)")
                .strategy(IdStrategy.None)
                .done()
                .property("fileSize")
                .notNull(true)
                .check("#{fileSize}>0")
                .comment("文件大小")
                .done()
                .done()
                .build();

        AbstractDAO abstractDAO = (AbstractDAO)dao;
        QuickDAOConfig quickDAOConfig = abstractDAO.quickDAOConfig;
        Assert.assertEquals(1,quickDAOConfig.entityMap.size());
        Entity entity = quickDAOConfig.entityMap.get(DownloadTask.class.getName());
        Assert.assertEquals("download_task",entity.tableName);
        Assert.assertEquals(quickDAOConfig.database.escape(entity.tableName),entity.escapeTableName);

        dao.rebuild(DownloadTask.class);
        {
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.setFilePath("c:/quickdao.jar");
            downloadTask.setFileSize(10000);
            downloadTask.setRemark("quickdao jar file");
            int effect = dao.insert(downloadTask);
            Assert.assertEquals(1,effect);
        }
        {
            DownloadTask downloadTask = dao.fetch(DownloadTask.class,"filePath","c:/quickdao.jar");
            Assert.assertNotNull(downloadTask);
        }
    }

    private DataSource[] getDataSources(){
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

//        DataSource[] dataSources = {mysqlDataSource, sqliteDataSource, h2DataSource, postgreDataSource};
        DataSource[] dataSources = {mysqlDataSource};
        return dataSources;
    }
}
