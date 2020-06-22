package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.dao.condition.Condition;
import cn.schoolwow.quickdao.dao.response.Response;
import cn.schoolwow.quickdao.dao.response.UnionType;
import cn.schoolwow.quickdao.dao.sql.transaction.Transaction;
import cn.schoolwow.quickdao.domain.PageVo;
import cn.schoolwow.quickdao.entity.Order;
import cn.schoolwow.quickdao.entity.Person;
import com.alibaba.fastjson.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.UUID;

@RunWith(Parameterized.class)
public class DAOTest extends BaseDAOTest{

    public DAOTest(DAO dao) {
        super(dao);
    }

    @Test
    public void testSQLDAO() {
        dao.rebuild(Person.class);
        dao.rebuild(Order.class);
        singleInsert();
        multiInsert();
        multiUpdate();
        updateByUniqueKey();
        updateById();
        save();
        delete();
        fetch();
    }

    @Test
    public void testQuery() {
        dao.rebuild(Person.class);
        dao.rebuild(Order.class);
        query();
        response();
    }

    @Test
    public void testTransaction() {
        dao.rebuild(Person.class);
        Transaction transaction = dao.startTransaction();
        {
            Person person = new Person();
            person.setFirstName("Bill");
            person.setLastName("Gates");
            person.setAddress("Xuanwumen 10");
            person.setCity("Beijing");
            transaction.insert(person);
            transaction.rollback();
            transaction.endTransaction();
        }
        {
            Person person = new Person();
            person.setLastName("Gates");
            boolean result = dao.exist(person);
            Assert.assertEquals(false,result);
        }
    }

    private void singleInsert() {
        {
            Person person = new Person();
            person.setFirstName("Bill");
            person.setLastName("Gates");
            person.setAddress("Xuanwumen 10");
            person.setCity("Beijing");
            int effect = dao.insert(person);
            Assert.assertEquals(1, effect);
        }
        {
            Person person = dao.fetch(Person.class,1);
            Assert.assertNotNull(person.getCreatedAt());
            Assert.assertNotNull(person.getUpdatedAt());
            Assert.assertEquals(true,person.getCreatedAt().getTime()-System.currentTimeMillis()<1000);
            Assert.assertEquals(true,person.getUpdatedAt().getTime()-System.currentTimeMillis()<1000);
        }
    }

    private void multiInsert() {
        Person person1 = new Person();
        person1.setFirstName("Thomas");
        person1.setLastName("Carter");
        person1.setAddress("Changan Street");
        person1.setCity("Beijing");

        Person person2 = new Person();
        person2.setLastName("Wilson");
        person2.setAddress("Champs-Elysees");

        Person[] persons = {person1, person2};
        int effect = dao.insert(persons);
        Assert.assertEquals(2, effect);
    }

    private void multiUpdate() {
        Person person1 = new Person();
        person1.setFirstName("Thomas");
        person1.setLastName("Carter");
        person1.setAddress("Changan Street 10");
        person1.setCity("Beijing");

        Person person2 = new Person();
        person2.setLastName("Wilson");
        person2.setAddress("Champs-Elysees 10");

        Person[] persons = {person1, person2};
        int effect = dao.update(persons);
        Assert.assertEquals(2, effect);
    }

    private void updateByUniqueKey() {
        {
            Person person = new Person();
            person.setLastName("Gates");
            person.setAddress("Xuanwumen 11");
            int effect = dao.update(person);
            Assert.assertEquals(1, effect);
        }
        {
            Person person = dao.fetch(Person.class,"lastName","Gates");
            Assert.assertEquals(true,System.currentTimeMillis()-person.getCreatedAt().getTime()<1000);
            Assert.assertEquals(true,System.currentTimeMillis()-person.getUpdatedAt().getTime()<1000);
        }
    }

    private void updateById() {
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setPersonId(1);
        order.setOrderNo(1);
        dao.insert(order);

        order.setOrderNo(2);
        int effect = dao.update(order);
        Assert.assertEquals(1, effect);
    }

    private void save() {
        {
            Person person = new Person();
            person.setFirstName("John");
            person.setLastName("Adams");
            person.setAddress("Oxford Street");
            person.setCity("London");
            int effect = dao.save(person);
            Assert.assertEquals(1, effect);
        }

        {
            Person person = new Person();
            person.setFirstName("Bill");
            person.setLastName("Gates");
            person.setAddress("Xuanwumen 100");
            person.setCity("TianJin");
            int effect = dao.save(person);
            Assert.assertEquals(1, effect);
        }
    }

    private void delete() {
        {
            int effect = dao.delete(Person.class,1);
            Assert.assertEquals(1,effect);
        }
        {
            int effect = dao.delete(Order.class,"personId",1);
            Assert.assertEquals(1,effect);
        }
    }

    private void fetch() {
        {
            Person person = dao.fetch(Person.class,2);
            Assert.assertNotNull(person);
            Assert.assertEquals("Carter",person.getLastName());
        }
        {
            Person person = dao.fetch(Person.class,"lastName","Adams");
            Assert.assertEquals("Adams",person.getLastName());
        }
        {
            List<Person> personList = dao.fetchList(Person.class,"city","Beijing");
            Assert.assertEquals(1,personList.size());
        }
    }

    private void query(){
        {
            Response response = dao.query(Person.class)
                    .distinct()
                    .addNullQuery("lastName")
                    .addNotNullQuery("lastName")
                    .addEmptyQuery("lastName")
                    .addNotEmptyQuery("lastName")
                    .addInQuery("lastName",new String[]{"1","2"})
                    .addNotInQuery("lastName",new String[]{"3","4"})
                    .addBetweenQuery("id",1,2)
                    .addLikeQuery("lastName","a")
                    .addQuery("lastName","=","a")
                    .compositField()
                    .joinTable(Order.class,"id","personId")
                    .addNullQuery("orderNo")
                    .addNotNullQuery("orderNo")
                    .addInQuery("orderNo",new Integer[]{1,2})
                    .addNotInQuery("orderNo",new Integer[]{1,2})
                    .addBetweenQuery("orderNo",1,2)
                    .addQuery("orderNo","=",1)
                    .done()
                    .execute();
            Assert.assertEquals(0,response.count());
        }
        {
            Condition condition = dao.query(Person.class)
                    .distinct()
                    .addQuery("firstName","a");
            condition.or()
                    .addQuery("lastName","a")
                    .addQuery("address","b")
                    .addQuery("city","c");
            Response response = condition.execute();
            Assert.assertEquals(0,response.getList().size());
        }
    }

    private void response(){
        {
            Person person = new Person();
            person.setFirstName("Bill");
            person.setLastName("Gates");
            person.setAddress("Xuanwumen 10");
            person.setCity("Beijing");
            int effect = dao.insert(person);
            Assert.assertEquals(1,effect);
        }
        {
            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setPersonId(1);
            order.setOrderNo(1);
            int effect = dao.insert(order);
            Assert.assertEquals(1,effect);
        }
        //全部字段查询
        {
            Condition condition = dao.query(Person.class)
                    .addQuery("lastName","Gates")
                    .addUpdate("address","Xuanwumen 11")
                    .groupBy("id")
                    .orderBy("lastName")
                    .page(1,10);
            Response response = condition.execute();
            Assert.assertEquals(1,response.count());
            Person person = (Person) response.getOne();
            Assert.assertEquals("Gates",person.getLastName());
            Assert.assertEquals(1,response.getList().size());
            {
                PageVo<Person> personPageVo = response.getPagingList();
                Assert.assertEquals(1,personPageVo.getCurrentPage());
                Assert.assertEquals(10,personPageVo.getPageSize());
                Assert.assertEquals(1,personPageVo.getTotalPage());
                Assert.assertEquals(1,personPageVo.getTotalSize());
                Assert.assertEquals("Gates",personPageVo.getList().get(0).getLastName());
            }
            Assert.assertEquals(1,response.update());
        }
        //单属性查询
        {
            Response response = dao.query(Person.class)
                    .addQuery("lastName","Gates")
                    .execute();
            List<Long> idList = response.getValueList(Long.class,"id");
            Assert.assertEquals(1,idList.size());
            Assert.assertEquals(1,idList.get(0).intValue());
        }
        //聚合查询
        {
            Response response = dao.query(Person.class)
                    .addAggregate("count","id")
                    .addAggregate("max","id","m(id)")
                    .groupBy("id")
                    .having("count","id",1)
                    .orderByDesc("m(id)")
                    .execute();
            JSONArray array = response.getAggerateList();
            //H2数据库所有返回的列名都是全大写
            Assert.assertEquals(1,array.getJSONObject(0).getInteger("count(id)").intValue());
            Assert.assertEquals(1,array.getJSONObject(0).getInteger("m(id)").intValue());
        }
        //关联查询
        {
            Response response = dao.query(Person.class)
                    .tableAliasName("p")
                    .addQuery("lastName","Gates")
                    .joinTable(Order.class,"id","personId")
                    .tableAliasName("o")
                    .done()
                    .compositField()
                    .execute();
            Assert.assertEquals(1,response.count());
            Person person = (Person) response.getOne();
            Assert.assertNotNull("关联查询订单实体类不能为空!",person.getOrder());
        }
        //部分查询
        {
            Response response = dao.query(Person.class)
                    .union(dao.query(Person.class)
                            .addQuery("lastName","Gates")
                            .addColumn(new String[]{"id","lastName","firstName"}))
                    .union(dao.query(Person.class)
                            .addQuery("firstName","Bill")
                            .addColumn(new String[]{"id","lastName","firstName"}),UnionType.UnionAll)
                    .addQuery("address","Xuanwumen 11")
                    .addColumn(new String[]{"id","lastName","firstName"})
                    .orderByDesc("id")
                    .page(1,10)
                    .execute();
            {
                List<Person> personList = response.getPartList();
                Assert.assertNull("部分查询结果城市字段应为空!",personList.get(0).getCity());
            }
            {
                PageVo<Person> personPageVo = response.getPartPagingList();
                Assert.assertEquals(1,personPageVo.getCurrentPage());
                Assert.assertEquals(10,personPageVo.getPageSize());
                Assert.assertEquals(1,personPageVo.getTotalPage());
                Assert.assertEquals(1,personPageVo.getTotalSize());
                Assert.assertNull(personPageVo.getList().get(0).getCity());
            }
            {
                List<Person> personList = response.getUnionList();
                Assert.assertEquals(2,personList.size());
            }
            {
                PageVo<Person> personPageVo = response.getUnionPagingList();
                Assert.assertEquals(1,personPageVo.getTotalSize());
            }
        }
        //部分查询
        {
            Response<Person> response = dao.query(Person.class)
                    .addQuery("lastName","Gates")
                    .excludeColumn("city")
                    .execute();
            Assert.assertNull("部分查询结果城市字段应为空!",response.getPartList().get(0).getCity());
        }
        //自定义列查询
        {
            Response response = dao.query(Person.class)
                    .addQuery("lastName","Gates")
                    .addSpecialColumn("max(t.id) as m")
                    .execute();
            JSONArray array = response.getSpecialList();
            Assert.assertEquals(1,array.size());
            Assert.assertEquals(1,array.getJSONObject(0).getIntValue("m"));
        }
        //删除
        {
            long effect = dao.query(Person.class).addQuery("lastName","Gates").execute().delete();
            Assert.assertEquals(1,effect);
        }
    }
}
