package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.domain.PageVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**虚表查询测试*/
@RunWith(Parameterized.class)
public class VirtualTest extends BaseDAOTest{

    public VirtualTest(DAO dao) {
        super(dao);
    }

    @Test
    public void testDML(){
        dao.refreshDbEntityList();
        {
            int effect = dao.query("product")
                    .addInsert("id",System.currentTimeMillis())
                    .addInsert("name","洗衣机")
                    .addInsert("type","家电")
                    .addInsert("price",1600)
                    .addInsert("person_id",1)
                    .execute()
                    .insert();
            Assert.assertEquals(1,effect);
        }
        {
            int effect = dao.query("product")
                    .addQuery("name","洗衣机")
                    .addQuery("type","家电")
                    .addUpdate("price",2000)
                    .execute()
                    .update();
            Assert.assertEquals(1,effect);
        }
    }

    @Test
    public void testDQL(){
        {
            int price = (int) dao.query("product")
                    .addQuery("name","冰箱")
                    .addColumn("price")
                    .execute()
                    .getSingleColumn(Integer.class);
            Assert.assertEquals(600,price);
        }
        {
            JSONObject result = dao.query("product")
                    .addQuery("type","电器")
                    .execute()
                    .getObject();
            Assert.assertEquals("电器",result.getString("type"));
        }
        {
            PageVo<JSONArray> pageVo = dao.query("product")
                    .addQuery("type","电器")
                    .page(1,2)
                    .execute()
                    .getPagingList();
            System.out.println(pageVo.getPageSize());
            System.out.println(pageVo.getCurrentPage());
            System.out.println(pageVo.getTotalPage());
            System.out.println(pageVo.getList());
        }
    }

    @Test
    public void testFetch(){
        JSONObject product = dao.fetch("product","type","数码");
        Assert.assertEquals(1000,product.getIntValue("price"));

        JSONArray productArray = dao.fetchList("product","type","电器");
        Assert.assertEquals(3,productArray.size());
        Assert.assertEquals("电器",productArray.getJSONObject(0).getString("type"));
    }

    @Test
    public void testDelete(){
        int effect = dao.delete("product","type","数码");
        Assert.assertEquals(1,effect);
    }
}