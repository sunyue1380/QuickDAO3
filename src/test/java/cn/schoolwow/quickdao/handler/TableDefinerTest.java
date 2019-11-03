package cn.schoolwow.quickdao.handler;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.entity.Person;
import org.junit.Assert;
import org.junit.Test;

public class TableDefinerTest {
    @Test
    public void testTableDefiner() throws Exception {
        QuickDAOConfig quickDAOConfig = new QuickDAOConfig();
        quickDAOConfig.packageNameMap.put("cn.schoolwow.quickdao.entity","");
        quickDAOConfig.defaultTableDefiner = new DefaultTableDefiner(null,quickDAOConfig);
        quickDAOConfig.defaultTableDefiner.getEntityMap();
        quickDAOConfig.defaultTableDefiner.define(Person.class)
                .tableName("t_person")
                .comment("人物")
                .done();
        Assert.assertEquals(2,quickDAOConfig.entityMap.size());
        Entity entity =  quickDAOConfig.entityMap.get(Person.class.getName());
        Assert.assertEquals("t_person",entity.tableName);
        Assert.assertEquals("人物",entity.comment);
    }

    @Test
    public void testTablePropertyDefiner() throws Exception {
        QuickDAOConfig quickDAOConfig = new QuickDAOConfig();
        quickDAOConfig.packageNameMap.put("cn.schoolwow.quickdao.entity","");
        quickDAOConfig.defaultTableDefiner = new DefaultTableDefiner(null,quickDAOConfig);
        quickDAOConfig.defaultTableDefiner.getEntityMap();
        quickDAOConfig.defaultTableDefiner.define(Person.class)
                .property("lastName")
                .comment("姓")
                .columnName("last_name")
                .columnType("varchar(128)")
                .notNull(true)
                .unique(true)
                .defaultValue("sun")
                .done();
        Assert.assertEquals(2,quickDAOConfig.entityMap.size());
        Entity entity =  quickDAOConfig.entityMap.get(Person.class.getName());
        for(Property property:entity.properties){
            if(property.name.equals("lastName")){
                Assert.assertEquals("姓",property.comment);
                Assert.assertEquals("last_name",property.column);
                Assert.assertEquals("varchar(128)",property.columnType);
                Assert.assertEquals(true,property.notNull);
                Assert.assertEquals(true,property.unique);
                Assert.assertEquals("sun",property.defaultValue);
            }
        }
    }

    @Test
    public void testGetEntityMap() throws Exception {
        QuickDAOConfig quickDAOConfig = new QuickDAOConfig();
        quickDAOConfig.packageNameMap.put("cn.schoolwow.quickdao.entity","");
        quickDAOConfig.defaultTableDefiner = new DefaultTableDefiner(null,quickDAOConfig);
        quickDAOConfig.defaultTableDefiner.getEntityMap();
        Entity entity = quickDAOConfig.entityMap.get(Person.class.getName());
        Assert.assertEquals("person",entity.tableName);
    }
}
