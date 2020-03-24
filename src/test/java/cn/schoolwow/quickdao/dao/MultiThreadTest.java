package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.dao.sql.transaction.Transaction;
import cn.schoolwow.quickdao.entity.Person;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@RunWith(Parameterized.class)
public class MultiThreadTest extends BaseDAOTest{

    public MultiThreadTest(DAO dao) {
        super(dao);
    }

    @Test
    public void testMultiThread() throws InterruptedException {
        ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        //多个线程重建表
        for(int i=0;i<5;i++){
            poolExecutor.execute(()->{
                dao.rebuild(Person.class);
            });
        }
        while(poolExecutor.getActiveCount()>0){
            System.out.println("当前活跃线程数:"+poolExecutor.getActiveCount()+",继续等待");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("当前活跃线程数:"+poolExecutor.getActiveCount()+",执行插入操作");
        //多个线程查询插入等
        for(int i=0;i<100;i++){
            final int index = i;
            poolExecutor.execute(()->{
                //插入
                {
                    Person person = new Person();
                    person.setFirstName("Bill");
                    person.setLastName("Gates"+index);
                    person.setAddress("Xuanwumen 10");
                    person.setCity("Beijing");

                    Person person2 = new Person();
                    person2.setFirstName("Thomas");
                    person2.setLastName("Carter"+index);
                    person2.setAddress("Changan Street");
                    person2.setCity("Beijing");

                    Person person3 = new Person();
                    person3.setLastName("Wilson"+index);
                    person3.setAddress("Champs-Elysees");

                    Person[] persons = new Person[]{person,person2,person3};
                    System.out.println("插入数:"+dao.insert(persons));
                }
                //开启事务
                Transaction transaction = dao.startTransaction();
                {
                    Person person = new Person();
                    person.setFirstName("Bill");
                    person.setLastName("Gates"+index);
                    person.setAddress("Xuanwumen 10");
                    person.setCity("Beijing");
                    transaction.save(person);
                    transaction.rollback();
                    transaction.endTransaction();
                }
            });
        }
        poolExecutor.shutdown();
        poolExecutor.awaitTermination(1, TimeUnit.HOURS);
        Assert.assertEquals(300,dao.query(Person.class).execute().count());
    }
}
