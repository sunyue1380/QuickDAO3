package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.entity.Order;
import cn.schoolwow.quickdao.entity.Person;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.UUID;

/**DML操作测试*/
@RunWith(Parameterized.class)
public class DMLTest extends BaseDAOTest{

    public DMLTest(DAO dao) {
        super(dao);
    }

    @Test
    public void testDMLOperation() {
        dao.rebuild(Person.class);
        dao.rebuild(Order.class);
        insert();
        update();
        save();
        delete();
    }

    private void insert() {
        //单独插入
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
        //批量插入
        {
            Person[] persons = new Person[5];
            for(int i=0;i<persons.length;i++){
                Person person = new Person();
                person.setPassword("123456");
                person.setFirstName("Jack");
                person.setLastName("Ma "+i);
                person.setAddress("Xuanwumen 10");
                person.setCity("Beijing");
                persons[i] = person;
            }
            int effect = dao.insert(persons);
            Assert.assertEquals(5, effect);
        }
        //无实体类插入
        {
            int effect = dao.query("order")
                    .addInsert("id",UUID.randomUUID().toString())
                    .addInsert("order_no",100)
                    .addInsert("person_id",100)
                    .execute()
                    .insert();
            Assert.assertEquals(1, effect);
        }

        //插入Order类
        {
            {
                Order order = new Order();
                order.setId(UUID.randomUUID().toString());
                order.setPersonId(1);
                order.setOrderNo(1);
                int effect = dao.insert(order);
                Assert.assertEquals(1, effect);
            }
            {
                Order[] orders = new Order[5];
                for(int i=0;i<orders.length;i++){
                    Order order = new Order();
                    order.setId(UUID.randomUUID().toString());
                    order.setPersonId(i+2);
                    order.setOrderNo(i+2);
                    orders[i] = order;
                }
                int effect = dao.insert(orders);
                Assert.assertEquals(5,effect);
            }
        }
    }

    private void update(){
        //根据唯一性约束更新
        {
            {
                Person person = new Person();
                person.setPassword("123456");
                person.setLastName("Gates");
                person.setAddress("Xuanwumen 11");
                int effect = dao.update(person);
                Assert.assertEquals(1, effect);
                person = dao.fetch(Person.class,"lastName","Gates");
                Assert.assertEquals("Xuanwumen 11", person.getAddress());
            }
            {
                Person[] persons = new Person[5];
                for(int i=0;i<persons.length;i++){
                    Person person = new Person();
                    person.setPassword("123456");
                    person.setFirstName("Jack");
                    person.setLastName("Ma "+i);
                    person.setAddress("Xuanwumen "+i);
                    person.setCity("Beijing");
                    persons[i] = person;
                }
                int effect = dao.update(persons);
                Assert.assertEquals(5, effect);
                List<Person> personList = dao.fetchList(Person.class,"firstName","Jack");
                Assert.assertEquals(5, personList.size());
                for(int i=0;i<personList.size();i++){
                    Person person = personList.get(i);
                    Assert.assertEquals("Jack",person.getFirstName());
                    Assert.assertEquals("Xuanwumen "+i,person.getAddress());
                }
            }
        }
        //根据id更新
        {
            {
                Order order = dao.fetch(Order.class,"orderNo",1);
                order.setPersonId(2);
                int effect = dao.update(order);
                Assert.assertEquals(1, effect);
                order = dao.fetch(Order.class,"orderNo",1);
                Assert.assertEquals(2, order.getPersonId());
            }
            {
                List<Order> orderList = dao.query(Order.class)
                        .addBetweenQuery("orderNo",2,7)
                        .execute()
                        .getList();
                for(Order order:orderList){
                    order.setPersonId(order.getOrderNo()+10);
                }
                int effect = dao.update(orderList);
                Assert.assertEquals(5, effect);
                orderList = dao.query(Order.class)
                        .addBetweenQuery("orderNo",2,7)
                        .execute()
                        .getList();
                for(Order order:orderList){
                    Assert.assertEquals(order.getOrderNo()+10,order.getPersonId());
                }
            }
        }
    }

    private void save() {
        //新增记录
        {
            long count = dao.query(Person.class).execute().count();
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("John");
            person.setLastName("Adams");
            person.setAddress("Oxford Street");
            person.setCity("London");
            int effect = dao.save(person);
            Assert.assertEquals(1, effect);
            Assert.assertEquals(count+1,dao.query(Person.class).execute().count());
        }
        //更新记录(根据唯一性约束更新)
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("Bill");
            person.setLastName("Adams");
            person.setAddress("Xuanwumen 100");
            person.setCity("TianJin");
            int effect = dao.save(person);
            Assert.assertEquals(1, effect);
            person = dao.fetch(Person.class,"lastName","Adams");
            Assert.assertEquals("Xuanwumen 100",person.getAddress());
        }
        //更新记录(根据id更新)
        {
            Order order = dao.fetch(Order.class,"orderNo",1);
            order.setPersonId(10);
            int effect = dao.save(order);
            Assert.assertEquals(1, effect);
            order = dao.fetch(Order.class,"orderNo",1);
            Assert.assertEquals(10, order.getPersonId());
        }
    }

    private void delete() {
        {
            long count = dao.query(Person.class).execute().count();
            int effect = dao.delete(Person.class,1);
            Assert.assertEquals(1,effect);
            Assert.assertEquals(count-1,dao.query(Person.class).execute().count());
        }
        {
            long count = dao.query(Order.class).execute().count();
            int effect = dao.delete(Order.class,"orderNo",1);
            Assert.assertEquals(1,effect);
            Assert.assertEquals(count-1,dao.query(Order.class).execute().count());
        }
        {
            long count = dao.query(Order.class).execute().count();
            int effect = dao.query(Order.class)
                    .addBetweenQuery("orderNo",2,7)
                    .execute()
                    .delete();
            Assert.assertEquals(5,effect);
            Assert.assertEquals(count-5,dao.query(Order.class).execute().count());
        }
    }
}
