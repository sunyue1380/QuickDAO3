package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.dao.condition.Condition;
import cn.schoolwow.quickdao.dao.sql.SQLDAOInvocationHandler;
import cn.schoolwow.quickdao.dao.sql.dml.AbstractDMLDAO;
import cn.schoolwow.quickdao.dao.sql.dml.DMLDAO;
import cn.schoolwow.quickdao.dao.sql.dql.AbstractDQLDAO;
import cn.schoolwow.quickdao.dao.sql.dql.DQLDAO;
import cn.schoolwow.quickdao.dao.sql.transaction.AbstractTransaction;
import cn.schoolwow.quickdao.dao.sql.transaction.Transaction;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import cn.schoolwow.quickdao.util.StringUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;

public class AbstractDAO implements DAO {
    private Logger logger = LoggerFactory.getLogger(DAO.class);
    //数据源配置信息
    public QuickDAOConfig quickDAOConfig;

    public AbstractDAO(QuickDAOConfig quickDAOConfig) {
        this.quickDAOConfig = quickDAOConfig;
    }

    @Override
    public boolean exist(Object instance) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.exist(instance);
    }

    @Override
    public boolean existAny(Object... instances) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.existAny(instances);
    }

    @Override
    public boolean existAll(Object... instances) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.existAll(instances);
    }

    @Override
    public boolean existAny(Collection instances) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.existAny(instances);
    }

    @Override
    public boolean existAll(Collection instances) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.existAll(instances);
    }

    @Override
    public <T> T fetch(Class<T> clazz, long id) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.fetch(clazz,id);
    }

    @Override
    public <T> T fetch(Class<T> clazz, String property, Object value) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.fetch(clazz,property,value);
    }

    @Override
    public <T> List<T> fetchList(Class<T> clazz, String property, Object value) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.fetchList(clazz,property,value);
    }

    @Override
    public JSONObject fetch(String tableName, String field, Object value) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.fetch(tableName,field,value);
    }

    @Override
    public JSONArray fetchList(String tableName, String field, Object value) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.fetchList(tableName,field,value);
    }

    @Override
    public Condition query(Class clazz) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.query(clazz);
    }

    @Override
    public Condition query(String tableName) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.query(tableName);
    }

    @Override
    public Condition query(Condition condition) {
        DQLDAO dqldao = createDQLDAO();
        return dqldao.query(condition);
    }

    @Override
    public int insert(Object instance) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.insert(instance);
    }

    @Override
    public int insert(Object[] instances) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.insert(instances);
    }

    @Override
    public int insert(Collection instanceCollection) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.insert(instanceCollection);
    }

    @Override
    public int insertBatch(Object[] instances) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.insertBatch(instances);
    }

    @Override
    public int insertBatch(Collection instanceCollection) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.insertBatch(instanceCollection);
    }

    @Override
    public int update(Object instance) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.update(instance);
    }

    @Override
    public int update(Object[] instances) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.update(instances);
    }

    @Override
    public int update(Collection instanceCollection) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.update(instanceCollection);
    }

    @Override
    public int save(Object instance) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.save(instance);
    }

    @Override
    public int save(Object[] instances) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.save(instances);
    }

    @Override
    public int save(Collection instanceCollection) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.save(instanceCollection);
    }

    @Override
    public int delete(Class delete, long id) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.delete(delete,id);
    }

    @Override
    public int delete(Class delete, String id) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.delete(delete,id);
    }

    @Override
    public int delete(Class clazz, String field, Object value) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.delete(clazz,field,value);
    }

    @Override
    public int delete(String tableName, String field, Object value) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.delete(tableName,field,value);
    }

    @Override
    public int clear(Class clazz) {
        DMLDAO dmldao = createDMLDAO();
        return dmldao.clear(clazz);
    }

    @Override
    public Transaction startTransaction() {
        quickDAOConfig.abstractDAO = this;
        AbstractTransaction transaction = new AbstractTransaction(quickDAOConfig);
        transaction.transaction = true;
        SQLDAOInvocationHandler sqldaoInvocationHandler = new SQLDAOInvocationHandler(transaction);
        Transaction transactionProxy = (Transaction) Proxy.newProxyInstance(Thread.currentThread()
                .getContextClassLoader(), new Class<?>[]{Transaction.class},sqldaoInvocationHandler);
        return transactionProxy;
    }

    @Override
    public boolean hasTable(String tableName) {
        for(Map.Entry<String,Entity> entry:quickDAOConfig.entityMap.entrySet()){
            if(entry.getValue().clazz.getSimpleName().equals(tableName)){
                tableName = entry.getValue().tableName;
                break;
            }
        }
        for(Entity dbEntity:quickDAOConfig.dbEntityList){
            if(dbEntity.tableName.equals(tableName)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void create(Class clazz) {
        create(this.quickDAOConfig.entityMap.get(clazz.getName()));
    }

    @Override
    public void create(Entity entity) {
        try {
            quickDAOConfig.tableBuilder.createTable(entity);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void drop(Class clazz) {
        try {
            quickDAOConfig.tableBuilder.dropTable(this.quickDAOConfig.entityMap.get(clazz.getName()).tableName);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void drop(String tableName) {
        try {
            quickDAOConfig.tableBuilder.dropTable(tableName);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void rebuild(Class clazz) {
        try {
            quickDAOConfig.tableBuilder.rebuild(this.quickDAOConfig.entityMap.get(clazz.getName()));
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void dropColumn(String tableName, String column) {
        Entity[] dbEntityList = quickDAOConfig.dbEntityList;
        for(Entity dbEntity:dbEntityList){
            if(dbEntity.tableName.equals(tableName)){
                for(Property property:dbEntity.properties){
                    if(property.column.equals(column)){
                        try {
                            quickDAOConfig.tableBuilder.deleteColumn(property);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
            }
        }
        logger.warn("[表名不存在或者该表不存在指定列!表名:{},列名:{}",tableName,column);
    }

    @Override
    public void refreshDbEntityList() {
        quickDAOConfig.tableBuilder.refreshDbEntityList();
    }

    @Override
    public void syncEntityList() {
        if(quickDAOConfig.packageNameMap.isEmpty()&&quickDAOConfig.entityClassMap.isEmpty()){
            throw new IllegalArgumentException("请先指定要扫描的实体类包或者实体类!");
        }
        try {
            quickDAOConfig.tableBuilder.automaticCreateTableAndField();
            //删除数据库多余的表和字段
            for(Entity dbEntity:quickDAOConfig.dbEntityList){
                boolean findTable = false;
                for(Entity entity:quickDAOConfig.entityMap.values()){
                    if(dbEntity.tableName.equals(entity.tableName)){
                        findTable = true;
                        for(Property dbProperty:dbEntity.properties){
                            boolean findProperty = false;
                            for(Property property:entity.properties){
                                if(dbProperty.column.equals(property.column)){
                                    findProperty = true;
                                    break;
                                }
                            }
                            if(!findProperty){
                                //删除多余的字段
                                dropColumn(dbEntity.tableName,dbProperty.column);
                            }
                        }
                        break;
                    }
                }
                if(!findTable){
                    //删除多余的表
                    drop(dbEntity.tableName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DataSource getDataSource() {
        return quickDAOConfig.dataSource;
    }

    @Override
    public Map<String, Entity> getEntityMap() {
        return quickDAOConfig.entityMap;
    }

    @Override
    public Entity[] getDbEntityList() {
        return quickDAOConfig.dbEntityList;
    }

    @Override
    public void generateEntityFile(String sourcePath, String[] tableNames) {
        if(quickDAOConfig.packageNameMap.isEmpty()){
            throw new IllegalArgumentException("请先调用packageName方法指定包名");
        }
        quickDAOConfig.autoCreateTable = false;
        quickDAOConfig.autoCreateProperty = false;
        //数据库类型对应表
        Map<String,String> mapping = new HashMap<>();
        mapping.put("varchar","String");
        mapping.put("longvarchar","String");
        mapping.put("text","String");
        mapping.put("mediumtext","String");
        mapping.put("longtext","String");
        mapping.put("boolean","boolean");
        mapping.put("tinyint","byte");
        mapping.put("blob","byte[]");
        mapping.put("char","String");
        mapping.put("smallint","short");
        mapping.put("int","int");
        mapping.put("integer","int");
        mapping.put("bigint","long");
        mapping.put("float","float");
        mapping.put("double","double");
        mapping.put("decimal","double");
        mapping.put("date","java.util.Date");
        mapping.put("time","java.util.Time");
        mapping.put("datetime","java.util.Date");
        mapping.put("timestamp","java.sql.Timestamp");

        List<Entity> dbEntityList;
        if(null==tableNames||tableNames.length==0){
            dbEntityList = Arrays.asList(quickDAOConfig.dbEntityList);
        }else{
            dbEntityList = new ArrayList<>(tableNames.length);
            for(String tableName:tableNames){
                for(Entity dbEntity:quickDAOConfig.dbEntityList){
                    if(dbEntity.tableName.equals(tableName)){
                        dbEntityList.add(dbEntity);
                        break;
                    }
                }
            }
        }
        StringBuilder builder = new StringBuilder();
        String packageName = quickDAOConfig.packageNameMap.keySet().iterator().next();
        for(Entity dbEntity:dbEntityList){
            dbEntity.className = StringUtil.Underline2Camel(dbEntity.tableName);
            dbEntity.className = dbEntity.className.toUpperCase().charAt(0)+dbEntity.className.substring(1);

            Path target = Paths.get(sourcePath+"/"+ packageName.replace(".","/") + "/" + dbEntity.className+".java");
            if(Files.exists(target)){
                logger.warn("[实体类文件已经存在]{}",target);
                continue;
            }

            builder.setLength(0);
            //新建Java类
            builder.append("package "+packageName+";\n");
            builder.append("import cn.schoolwow.quickdao.annotation.*;\n\n");
            if(null!=dbEntity.comment){
                builder.append("@Comment(\""+dbEntity.comment+"\")\n");
            }
            if(null!=dbEntity.tableName){
                builder.append("@TableName(\""+dbEntity.tableName+"\")\n");
            }
            builder.append("public class "+dbEntity.className+"{\n\n");
            for(Property property:dbEntity.properties){
                if(null!=property.comment&&!property.comment.isEmpty()){
                    builder.append("\t@Comment(\""+property.comment.replaceAll("\r\n","")+"\")\n");
                }
                if(property.id){
                    if(property.strategy.equals(IdStrategy.AutoIncrement)){
                        builder.append("\t@Id\n");
                    }else{
                        builder.append("\t@Id(strategy = IdStrategy.None)\n");
                    }
                }
                builder.append("\t@ColumnName(\""+property.column+"\")\n");
                builder.append("\t@ColumnType(\""+property.columnType+"\")\n");
                if(property.columnType.contains("(")){
                    property.columnType = property.columnType.substring(0,property.columnType.indexOf("("));
                }
                property.className = mapping.get(property.columnType.toLowerCase());
                property.name = StringUtil.Underline2Camel(property.column);
                builder.append("\tprivate "+property.className+" "+property.name+";\n\n");
            }

            for(Property property:dbEntity.properties){
                builder.append("\tpublic "+ property.className +" get" +StringUtil.firstLetterUpper(property.name)+"(){\n\t\treturn this."+property.name+";\n\t}\n\n");
                builder.append("\tpublic void set" +StringUtil.firstLetterUpper(property.name)+"("+property.className+" "+property.name+"){\n\t\tthis."+property.name+"= "+property.name+";\n\t}\n\n");
            }

            builder.append("};");

            ByteArrayInputStream bais = new ByteArrayInputStream(builder.toString().getBytes());
            try {
                Files.createDirectories(target.getParent());
                Files.copy(bais, target);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**创建DMLDAO*/
    private DMLDAO createDMLDAO(){
        quickDAOConfig.abstractDAO = this;
        AbstractDMLDAO abstractDMLDAO = new AbstractDMLDAO(quickDAOConfig);
        SQLDAOInvocationHandler sqldaoInvocationHandler = new SQLDAOInvocationHandler(abstractDMLDAO);
        DMLDAO dmldao = (DMLDAO) Proxy.newProxyInstance(Thread.currentThread()
                .getContextClassLoader(), new Class<?>[]{DMLDAO.class},sqldaoInvocationHandler);
        return dmldao;
    }

    /**创建DQLDAO*/
    private DQLDAO createDQLDAO(){
        quickDAOConfig.abstractDAO = this;
        AbstractDQLDAO abstractDQLDAO = new AbstractDQLDAO(quickDAOConfig);
        SQLDAOInvocationHandler sqldaoInvocationHandler = new SQLDAOInvocationHandler(abstractDQLDAO);
        DQLDAO dqldao = (DQLDAO) Proxy.newProxyInstance(Thread.currentThread()
                .getContextClassLoader(), new Class<?>[]{DQLDAO.class},sqldaoInvocationHandler);
        return dqldao;
    }
}
