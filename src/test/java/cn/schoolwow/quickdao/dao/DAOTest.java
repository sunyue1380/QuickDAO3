package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.dao.condition.Condition;
import cn.schoolwow.quickdao.dao.response.Response;
import cn.schoolwow.quickdao.dao.response.UnionType;
import cn.schoolwow.quickdao.dao.sql.transaction.Transaction;
import cn.schoolwow.quickdao.domain.PageVo;
import cn.schoolwow.quickdao.entity.Order;
import cn.schoolwow.quickdao.entity.Person;
import cn.schoolwow.quickdao.entity.Product;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Date;
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
        dao.rebuild(Product.class);
        singleInsert();
        multiInsert();
        multiUpdate();
        updateByUniqueKey();
        updateById();
        save();
        delete();
        fetch();
        cloneTest();
    }

    @Test
    public void testQuery() {
        dao.rebuild(Person.class);
        dao.rebuild(Order.class);
        dao.rebuild(Product.class);
        query();
        response();
    }

    @Test
    public void testTransaction() {
        dao.rebuild(Person.class);
        Transaction transaction = dao.startTransaction();
        {
            Person person = new Person();
            person.setPassword("123456");
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

    @Test
    public void testSubQuery(){
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
            transaction.insert(product);
        }
        transaction.commit();

        testWhereSubQuery();
        testHavingSubQuery();
        testFromSubQuery();
        testSelectSubQuery();
        testExistSubQuery();
    }

    private void testExistSubQuery(){
        List<String> productNameList = dao.query(Product.class)
                .addExistSubQuery(
                        dao.query(Product.class)
                                .addQuery("price",">=",5000)
                                .addColumn("id")
                )
                .execute()
                .getSingleColumnList(String.class);
        Assert.assertEquals(0,productNameList.size());
    }

    private void testSelectSubQuery(){
        Condition selectCondition = dao.query("dual")
                .addColumn("name");
        List<String> productNameList = dao.query(Product.class)
                .addColumn(selectCondition,"nameAlias")
                .execute()
                .getSingleColumnList(String.class);
        Assert.assertEquals(4,productNameList.size());
    }

    private void testFromSubQuery(){
        Condition<Product> fromCondition = dao.query(Product.class)
                .groupBy("type")
                .addColumn("type")
                .addColumn("avg(price) avgPrice");
        JSONArray array = dao.query(fromCondition)
                .addQuery("avgPrice",">=",2000)
                .addColumn("type","avgPrice")
                .execute()
                .getArray();
        Assert.assertEquals(1,array.size());
    }

    private void testHavingSubQuery(){
        Condition havingCondition = dao.query("dual")
                .addColumn("1");
        long count = (long) dao.query(Product.class)
                .groupBy("type")
                .having("count(type)",">",havingCondition)
                .addColumn("count(type) count")
                .execute()
                .getSingleColumn(Long.class);
        Assert.assertEquals(3,count);
    }

    private void testWhereSubQuery(){
        long count = dao.query(Product.class)
                .addSubQuery("price","<",dao.query(Product.class).addColumn("avg(price)"))
                .execute()
                .count();
        Assert.assertEquals(2,count);
    }

    private void singleInsert() {
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
            Assert.assertNotNull(person.getCreatedAt());
            Assert.assertNotNull(person.getUpdatedAt());
            Assert.assertEquals(true,person.getCreatedAt().getTime()-System.currentTimeMillis()<1000);
            Assert.assertEquals(true,person.getUpdatedAt().getTime()-System.currentTimeMillis()<1000);
        }
        {
            Product product = new Product();
            product.setName("笔记本电脑");
            int effect = dao.insert(product);
            Assert.assertEquals(1, effect);
            Assert.assertTrue(dao.exist(product));
            Assert.assertTrue(product.getId()>0);
        }
        {
            int effect = dao.query("product")
                    .addInsert("id","123456789")
                    .addInsert("name","平板电脑")
                    .addInsert("publish_time",new Date())
                    .execute()
                    .insert();
            Assert.assertEquals(1, effect);
        }
    }

    private void multiInsert() {
        Person person1 = new Person();
        person1.setPassword("123456");
        person1.setFirstName("Thomas");
        person1.setLastName("Carter");
        person1.setAddress("Changan Street");
        person1.setCity("Beijing");

        Person person2 = new Person();
        person2.setPassword("123456");
        person2.setLastName("Wilson");
        person2.setAddress("Champs-Elysees");

        Person[] persons = {person1, person2};
        int effect = dao.insert(persons);
        Assert.assertEquals(2, effect);
    }

    private void multiUpdate() {
        Person person1 = new Person();
        person1.setPassword("123456");
        person1.setFirstName("Thomas");
        person1.setLastName("Carter");
        person1.setAddress("Changan Street 10");
        person1.setCity("Beijing");

        Person person2 = new Person();
        person2.setPassword("123456");
        person2.setLastName("Wilson");
        person2.setAddress("Champs-Elysees 10");

        Person[] persons = {person1, person2};
        int effect = dao.update(persons);
        Assert.assertEquals(2, effect);
    }

    private void updateByUniqueKey() {
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setLastName("Gates");
            person.setAddress("Xuanwumen 11");
            int effect = dao.update(person);
            Assert.assertEquals(1, effect);
        }
        {
            int effect = dao.query("person")
                    .addQuery("last_name","Gates")
                    .addUpdate("password","654321")
                    .addUpdate("address","Xuanwumen 11")
                    .execute()
                    .update();
            Assert.assertEquals(1, effect);
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
            person.setPassword("123456");
            person.setFirstName("John");
            person.setLastName("Adams");
            person.setAddress("Oxford Street");
            person.setCity("London");
            int effect = dao.save(person);
            Assert.assertEquals(1, effect);
        }

        {
            Person person = new Person();
            person.setPassword("123456");
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

    private void cloneTest() {
        Condition<Person> condition = dao.query(Person.class).addQuery("password","123456");
        {
            Person person = condition.clone().addQuery("lastName","Carter").execute().getOne();
            Assert.assertEquals("Beijing",person.getCity());
        }
        {
            Person person = condition.clone().addQuery("lastName","Adams").execute().getOne();
            Assert.assertEquals("London",person.getCity());
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
                    .addInQuery("lastName","1","2")
                    .addNotInQuery("lastName","3","4")
                    .addBetweenQuery("id",1,2)
                    .addLikeQuery("lastName","%a%")
                    .addQuery("lastName","=","a")
                    .addQuery("updatedAt","<=",new Date())
                    .compositField()
                    .joinTable(Order.class,"id","personId")
                    .addNullQuery("orderNo")
                    .addNotNullQuery("orderNo")
                    .addInQuery("orderNo",1,2)
                    .addNotInQuery("orderNo",1,2)
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
        {
            Condition condition = dao.query(Person.class)
                    .distinct()
                    .addQuery("firstName","a")
                    .or("t.last_name = ?","a")
                    .or("t.address = ?","b")
                    .or("t.city = ?","c");
            Response response = condition.execute();
            Assert.assertEquals(0,response.getList().size());
        }
    }

    private void response(){
        {
            Person person = new Person();
            person.setPassword("123456");
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
                    .addColumn("t.id")
                    .execute();
            List<Long> idList = response.getSingleColumnList(Long.class);
            Assert.assertEquals(1,idList.size());
            Assert.assertEquals(1,idList.get(0).intValue());
        }
        //聚合查询
        {

            Response response = dao.query(Person.class)
                    .addColumn("COUNT(ID) as count")
                    .addColumn("max(id) as \"M(ID)\"")
                    .groupBy("id")
                    .having("count(id) = 1",null)
                    .orderByDesc("max(id)")
                    .execute();
            JSONArray array = response.getArray();
            //H2数据库所有返回的列名都是全大写
            Assert.assertEquals(1,array.getJSONObject(0).getInteger("count").intValue());
            Assert.assertEquals(1,array.getJSONObject(0).getInteger("M(ID)").intValue());
        }
        //聚合查询
        {
            Response response = dao.query(Person.class)
                    .joinTable(Order.class,"id","personId")
                    .groupBy("id")
                    .done()
                    .addColumn("t1.id")
                    .orderByDesc("t1.id")
                    .execute();
            Assert.assertEquals(1,response.count());
        }
        //关联查询
        {
            Response response = dao.query(Person.class)
                    .tableAliasName("p")
                    .addQuery("lastName","Gates")
                    .joinTable(Order.class,"id","personId")
                    .tableAliasName("o")
                    .orderByDesc("id")
                    .done()
                    .compositField()
                    .execute();
            Assert.assertEquals(1,response.count());
            Person person = (Person) response.getOne();
            Assert.assertNotNull("关联查询订单实体类不能为空!",person.getOrder());
        }
        //关联查询
        {
            Response response = dao.query(Person.class)
                    .joinTable(Order.class,"id","personId")
                    .addQuery("orderNo",1)
                    .orderByDesc("id")
                    .done()
                    .compositField()
                    .execute();
            Assert.assertEquals(1,response.count());
            Person person = (Person) response.getOne();
            Assert.assertNotNull("关联查询订单实体类不能为空!",person.getOrder());
        }
        //关联查询
        {
            Condition joinCondition = dao.query(Order.class)
                    .addColumn("person_id","count(person_id) count")
                    .groupBy("personId")
                    .having("count(person_id) > 0");
            Response response = dao.query(Person.class)
                    .joinTable(joinCondition,
                            "id","person_id")
                    .done()
                    .orderByDesc("id")
                    .execute();
            Assert.assertEquals(1,response.count());
        }
        //cross join查询
        {
            List<Person> personList = dao.query(Person.class)
                    .crossJoinTable(Order.class)
                    .addQuery("id",">",0)
                    .done()
                    .addRawQuery("t.id = t1.person_id")
                    .execute()
                    .getList();
            Assert.assertEquals(0,personList.size());
            personList = dao.query("person")
                    .crossJoinTable("order")
                    .addQuery("id",">",0)
                    .done()
                    .addRawQuery("t.id = t1.person_id")
                    .execute()
                    .getList();
            Assert.assertEquals(0,personList.size());
        }
        //部分查询
        {
            Response response = dao.query(Person.class)
                    .union(dao.query(Person.class)
                            .addQuery("last_name","Gates")
                            .addColumn("id","last_name","first_name"))
                    .union(dao.query(Person.class)
                            .addQuery("first_name","Bill")
                            .addColumn("id","last_name","first_name"),UnionType.UnionAll)
                    .addQuery("address","Xuanwumen 11")
                    .addColumn("id","last_name","first_name")
                    .orderByDesc("id")
                    .page(1,10)
                    .execute();
            {
                List<Person> personList = response.getList();
                Assert.assertNull("部分查询结果城市字段应为空!",personList.get(0).getCity());
            }
            {
                PageVo<Person> personPageVo = response.getPagingList();
                Assert.assertEquals(1,personPageVo.getCurrentPage());
                Assert.assertEquals(10,personPageVo.getPageSize());
                Assert.assertEquals(1,personPageVo.getTotalPage());
                Assert.assertEquals(1,personPageVo.getTotalSize());
                Assert.assertNull(personPageVo.getList().get(0).getCity());
            }
            {
                List<Person> personList = response.getList();
                Assert.assertEquals(2,personList.size());
            }
            {
                PageVo<Person> personPageVo = response.getPagingList();
                Assert.assertEquals(1,personPageVo.getTotalSize());
            }
        }
        //自定义列查询
        {
            Response response = dao.query(Person.class)
                    .addQuery("lastName","Gates")
                    .addColumn("max(t.id) as M")
                    .execute();
            JSONArray array = response.getArray();
            Assert.assertEquals(1,array.size());
            JSONObject o = array.getJSONObject(0);
            if(o.containsKey("M")){
                Assert.assertEquals(1,o.getIntValue("M"));
            }else if(o.containsKey("m")){
                Assert.assertEquals(1,o.getIntValue("m"));
            }
        }
        //删除
        {
            long effect = dao.query(Person.class).addQuery("lastName","Gates").execute().delete();
            Assert.assertEquals(1,effect);
        }
    }
}
