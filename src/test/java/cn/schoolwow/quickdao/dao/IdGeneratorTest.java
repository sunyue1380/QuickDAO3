package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.QuickDAO;
import cn.schoolwow.quickdao.dao.sql.dml.IDGenerator;
import cn.schoolwow.quickdao.dao.sql.dml.SnowflakeIdGenerator;
import cn.schoolwow.quickdao.entity.Person;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdGeneratorTest {
    private Logger logger = LoggerFactory.getLogger(IdGeneratorTest.class);
    @Test
    public void testGenerator(){
        IDGenerator[] idGenerators = new IDGenerator[]{
                new SnowflakeIdGenerator()
        };
        for(IDGenerator idGenerator:idGenerators){
            long startTime = System.currentTimeMillis();
            long[] ids = new long[100000];
            for(int i=0;i<ids.length;i++){
                ids[i] = idGenerator.getNextId();
            }
            long endTime = System.currentTimeMillis();
            logger.info("[{}]生成{}个id用时:{}ms",idGenerator.getClass().getSimpleName(),ids.length,endTime-startTime);
        }
    }

    @Test
    public void testSnowFlakeIdGenerator(){
        BasicDataSource mysqlDataSource = new BasicDataSource();
        mysqlDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        mysqlDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/quickdao");
        mysqlDataSource.setUsername("root");
        mysqlDataSource.setPassword("123456");

        DAO dao = QuickDAO.newInstance().dataSource(mysqlDataSource)
                .packageName("cn.schoolwow.quickdao.entity")
                .autoCreateTable(true)
                .idGenerator(new SnowflakeIdGenerator())
                .build();
        Person person = new Person();
        person.setFirstName("Bill");
        person.setLastName("Gates");
        person.setAddress("Xuanwumen 10");
        person.setCity("Beijing");
        Assert.assertEquals(1,dao.insert(person));
    }
}
