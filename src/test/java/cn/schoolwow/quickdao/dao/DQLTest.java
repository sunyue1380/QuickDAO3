package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.dao.condition.Condition;
import cn.schoolwow.quickdao.dao.response.Response;
import cn.schoolwow.quickdao.domain.PageVo;
import cn.schoolwow.quickdao.entity.Order;
import cn.schoolwow.quickdao.entity.Person;
import cn.schoolwow.quickdao.entity.Product;
import com.alibaba.fastjson.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**DQL操作测试*/
@RunWith(Parameterized.class)
public class DQLTest extends BaseDAOTest{

    public DQLTest(DAO dao) {
        super(dao);
    }

    @Test
    public void testDQLOperation(){
        initialize();
        fetch();
        query();
        page();
        joinTable();
        groupBy();
        column();
        cloneable();
    }

    private void fetch(){
        {
            Person person = dao.fetch(Person.class,1);
            Assert.assertEquals("Gates",person.getLastName());
        }
        {
            Person person = dao.fetch(Person.class,"lastName","Carter");
            Assert.assertEquals("Thomas",person.getFirstName());
        }
        {
            List<Person> personList = dao.fetchList(Person.class,"password","123456");
            Assert.assertEquals(3,personList.size());
        }
    }

    /**查询条件*/
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
                    .execute();
            Assert.assertEquals(0,response.count());
        }
        {
            long count = dao.query(Person.class)
                    .addQuery("password","123456")
                    .execute()
                    .count();
            Assert.assertEquals(3,count);
        }
        //or查询
        {
            Condition condition = dao.query(Person.class)
                    .distinct()
                    .addQuery("lastName","Gates");
            condition.or().addQuery("lastName","Carter");
            condition.or().addQuery("lastName","Wilson");
            Assert.assertEquals(3,condition.execute().count());
        }
        {
            Condition condition = dao.query(Person.class)
                    .distinct()
                    .addQuery("lastName","Gates")
                    .or("t.last_name = ?","Carter")
                    .or("t.last_name = ?","Wilson");
            Assert.assertEquals(3,condition.execute().count());
        }
    }

    private void page(){
        {
            Product product = (Product) dao.query(Product.class)
                    .orderByDesc("price")
                    .limit(0,1)
                    .execute()
                    .getOne();
            Assert.assertEquals(4000,product.getPrice());
        }
        {
            PageVo<Product> productPageVo = dao.query(Product.class)
                    .page(1,10)
                    .execute()
                    .getPagingList();
            Assert.assertEquals(1,productPageVo.getCurrentPage());
            Assert.assertEquals(10,productPageVo.getPageSize());
            Assert.assertEquals(1,productPageVo.getTotalPage());
            Assert.assertEquals(4,productPageVo.getTotalSize());
            Assert.assertEquals(4,productPageVo.getList().size());
        }
    }

    private void joinTable() {
        {
            List<Product> productList = dao.query(Product.class)
                    .joinTable(Person.class,"personId","id")
                    .done()
                    .compositField()
                    .execute()
                    .getList();
            Assert.assertEquals(4,productList.size());
            Assert.assertNotNull(productList.get(0).getPerson());
        }
        {
            List<Product> productList = dao.query(Product.class)
                    .joinTable(Person.class,"personId","id","person")
                    .done()
                    .compositField()
                    .execute()
                    .getList();
            Assert.assertEquals(4,productList.size());
            for(Product product:productList){
                Assert.assertNotNull(product.getPerson());
            }
        }
        {
            List<Product> productList = dao.query(Product.class)
                    .joinTable("person","personId","id")
                    .addQuery("last_name","Carter")
                    .done()
                    .execute()
                    .getList();
            Assert.assertEquals(0,productList.size());
        }
        {
            List<Product> productList = dao.query(Product.class)
                    .joinTable("person","personId","id")
                    .addQuery("last_name","Carter")
                    .leftJoin()
                    .done()
                    .execute()
                    .getList();
            Assert.assertEquals(4,productList.size());
        }
        {
            Condition joinCondition = dao.query(Person.class)
                    .addQuery("last_name","Gates")
                    .addColumn("id");
            List<Product> productList = dao.query(Product.class)
                    .joinTable(joinCondition,"personId","id")
                    .done()
                    .execute()
                    .getList();
            Assert.assertEquals(4,productList.size());
        }
        {
            List<Product> productList = dao.query(Product.class)
                    .crossJoinTable(Person.class)
                    .done()
                    .addRawQuery("t.person_id = t1.id")
                    .execute()
                    .getList();
            Assert.assertEquals(4,productList.size());
        }
        {
            List<Product> productList = dao.query(Product.class)
                    .crossJoinTable("person")
                    .done()
                    .addRawQuery("t.person_id = t1.id")
                    .execute()
                    .getList();
            Assert.assertEquals(4,productList.size());
        }
    }

    private void groupBy(){
        {
            //查询所有商品平均价格
            Long avgPrice = (Long) dao.query(Product.class)
                    .addColumn("round(avg(price)) avgPrice")
                    .groupBy("price")
                    .execute()
                    .getSingleColumn(Long.class);
            Assert.assertEquals(600,avgPrice.longValue());
        }
        {
            //根据类型分组,查询各个分组的平均价格
            JSONArray array = dao.query(Product.class)
                    .addColumn("type","round(avg(price)) avgPrice")
                    .groupBy("type")
                    .orderByDesc("round(avg(price))")
                    .execute()
                    .getArray();
            Assert.assertEquals(2,array.size());
            Assert.assertEquals(2533,array.getJSONObject(0).getIntValue("avgPrice"));
            Assert.assertEquals("电器",array.getJSONObject(0).getString("type"));
            Assert.assertEquals(1000,array.getJSONObject(1).getIntValue("avgPrice"));
            Assert.assertEquals("数码",array.getJSONObject(1).getString("type"));
        }
        {
            //根据类型分组,查询分组个数大于1的分组
            JSONArray array = dao.query(Product.class)
                    .addColumn("type","count(id) count")
                    .groupBy("type")
                    .having("count(id) > 1")
                    .execute()
                    .getArray();
            Assert.assertEquals(1,array.size());
            Assert.assertEquals(3,array.getJSONObject(0).getIntValue("count"));
            Assert.assertEquals("电器",array.getJSONObject(0).getString("type"));
        }
    }

    private void column(){
        List<Person> personList = dao.query(Person.class)
                .addColumn("lastName","firstName")
                .execute()
                .getList();
        Assert.assertEquals(3,personList.size());
        for(Person person:personList){
            Assert.assertNull(person.getAddress());
            Assert.assertNull(person.getCity());
        }
    }

    private void cloneable() {
        Condition<Person> condition = dao.query(Person.class).addQuery("password","123456");
        {
            Person person = condition.clone().addQuery("lastName","Gates").execute().getOne();
            Assert.assertEquals("Beijing",person.getCity());
        }
        {
            Person person = condition.clone().addQuery("lastName","Carter").execute().getOne();
            Assert.assertEquals("Beijing",person.getCity());
        }
    }

    private void initialize(){
        dao.rebuild(Person.class);
        dao.rebuild(Order.class);
        Person[] persons = new Person[3];
        //初始化数据
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("Bill");
            person.setLastName("Gates");
            person.setAddress("Xuanwumen 10");
            person.setCity("Beijing");
            persons[0] = person;
        }
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("Thomas");
            person.setLastName("Carter");
            person.setAddress("Changan Street");
            person.setCity("Beijing");
            persons[1] = person;
        }
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setLastName("Wilson");
            person.setAddress("Champs-Elysees");
            persons[2] = person;
        }
        {
            int effect = dao.insert(persons);
            Assert.assertEquals(3, effect);
        }
        {
            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setPersonId(1);
            order.setOrderNo(1);
            int effect = dao.insert(order);
            Assert.assertEquals(1, effect);
        }
    }
}
