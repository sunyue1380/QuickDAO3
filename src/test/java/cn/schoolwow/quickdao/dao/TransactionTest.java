package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.dao.sql.transaction.Transaction;
import cn.schoolwow.quickdao.entity.Person;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**事务测试*/
@RunWith(Parameterized.class)
public class TransactionTest extends BaseDAOTest {

    public TransactionTest(DAO dao) {
        super(dao);
    }

    @Test
    public void testTransaction(){
        dao.rebuild(Person.class);
        commit();
        rollback();
    }

    private void commit(){
        {
            Person person = dao.fetch(Person.class,"lastName","Gates");
            Assert.assertNull(person);
        }

        //提交事务
        Transaction transaction = dao.startTransaction();
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("Bill");
            person.setLastName("Gates");
            person.setAddress("Xuanwumen 10");
            person.setCity("Beijing");
            transaction.insert(person);

            person = dao.fetch(Person.class,"lastName","Gates");
            Assert.assertNull(person);

            transaction.commit();
            person = dao.fetch(Person.class,"lastName","Gates");
            Assert.assertNotNull(person);
        }
        transaction.endTransaction();
    }

    private void rollback(){
        {
            Person person = dao.fetch(Person.class,"lastName","Carter");
            Assert.assertNull(person);
        }

        //提交事务
        Transaction transaction = dao.startTransaction();
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("Thomas");
            person.setLastName("Carter");
            person.setAddress("Changan Street");
            person.setCity("Beijing");

            person = dao.fetch(Person.class,"lastName","Carter");
            Assert.assertNull(person);

            transaction.rollback();
            person = dao.fetch(Person.class,"lastName","Carter");
            Assert.assertNull(person);
        }
        transaction.endTransaction();
    }
}
