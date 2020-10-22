package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.entity.Person;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**测试注解是否正常*/
@RunWith(Parameterized.class)
public class AnnotationTest extends BaseDAOTest{

    public AnnotationTest(DAO dao) {
        super(dao);
    }

    @Test
    public void testTableField(){
        dao.rebuild(Person.class);
        //function
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("Bill");
            person.setLastName("Gates");
            person.setAddress("Xuanwumen 10");
            person.setCity("Beijing");
            int effect = dao.insert(person);
            Assert.assertEquals(1, effect);

            person = dao.fetch(Person.class,1);
            Assert.assertEquals("7199e4df92df94bb67252513b070945c", person.getPassword());
        }
        //createdAt
        {
            Person person = dao.fetch(Person.class,1);
            Assert.assertTrue(System.currentTimeMillis()-person.getCreatedAt().getTime()<1000);
        }
        //updatedAt
        {
            Person person = dao.fetch(Person.class,1);
            person.setAddress("Xuanwumen 11");
            int effect = dao.update(person);
            Assert.assertEquals(1, effect);

            person = dao.fetch(Person.class,1);
            Assert.assertTrue(System.currentTimeMillis()-person.getUpdatedAt().getTime()<1000);
        }
    }
}
