package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.dao.condition.Condition;
import cn.schoolwow.quickdao.dao.sql.transaction.Transaction;
import cn.schoolwow.quickdao.entity.Product;
import com.alibaba.fastjson.JSONArray;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Date;
import java.util.List;

/**子查询测试*/
@RunWith(Parameterized.class)
public class SubQueryTest extends BaseDAOTest{

    public SubQueryTest(DAO dao) {
        super(dao);
    }

    @Test
    public void testSubQuery(){
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
                .addColumn("name")
                .execute()
                .getSingleColumnList(String.class);
        Assert.assertEquals(0,productNameList.size());
    }

    private void testSelectSubQuery(){
        Condition selectCondition = dao.query(Product.class)
                .addQuery("type","电器")
                .addQuery("price",4000)
                .addColumn("name");
        List<String> productNameList = dao.query(Product.class)
                .addColumn(selectCondition,"nameAlias")
                .execute()
                .getSingleColumnList(String.class);
        Assert.assertEquals(4,productNameList.size());
    }

    private void testFromSubQuery(){
        Condition<Product> fromCondition = dao.query(Product.class)
                .addQuery("type","电器")
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
}
