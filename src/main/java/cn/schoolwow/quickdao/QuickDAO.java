package cn.schoolwow.quickdao;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.builder.table.*;
import cn.schoolwow.quickdao.dao.AbstractDAO;
import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.dao.SQLiteDAO;
import cn.schoolwow.quickdao.dao.sql.dml.IDGenerator;
import cn.schoolwow.quickdao.database.*;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import cn.schoolwow.quickdao.handler.DefaultTableDefiner;
import cn.schoolwow.quickdao.handler.TableDefiner;
import cn.schoolwow.quickdao.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Predicate;

public class QuickDAO {
    private Logger logger = LoggerFactory.getLogger(QuickDAO.class);
    private QuickDAOConfig quickDAOConfig = new QuickDAOConfig();

    public static QuickDAO newInstance() {
        return new QuickDAO();
    }

    public QuickDAO(){
        quickDAOConfig.defaultTableDefiner = new DefaultTableDefiner(this,quickDAOConfig);
    }

    /**
     * 设置数据库连接池
     * @param dataSource 数据库连接池
     * */
    public QuickDAO dataSource(DataSource dataSource) {
        quickDAOConfig.dataSource = dataSource;
        return this;
    }

    /**
     * 待扫描实体类包名,支持嵌套扫描
     * @param packageName 实体类所在包名
     * */
    public QuickDAO packageName(String packageName) {
        quickDAOConfig.packageNameMap.put(packageName, "");
        return this;
    }

    /**
     * 待扫描实体类包名,支持嵌套扫描
     * @param packageName 实体类所在包名
     * @param prefix 表前缀
     * */
    public QuickDAO packageName(String packageName, String prefix) {
        quickDAOConfig.packageNameMap.put(packageName, prefix + "_");
        return this;
    }

    /**
     * 待扫描实体类包名,支持嵌套扫描
     * @param entityClass 实体类
     * */
    public QuickDAO entity(Class entityClass) {
        quickDAOConfig.entityClassMap.put(entityClass,"");
        return this;
    }

    /**
     * 待扫描实体类包名,支持嵌套扫描
     * @param entityClass 实体类
     * @param prefix 表前缀
     * */
    public QuickDAO entity(Class entityClass, String prefix) {
        quickDAOConfig.entityClassMap.put(entityClass,prefix);
        return this;
    }

    /**
     * 忽略包名
     * @param ignorePackageName 扫描实体类时需要忽略的包
     * */
    public QuickDAO ignorePackageName(String ignorePackageName) {
        if (quickDAOConfig.ignorePackageNameList == null) {
            quickDAOConfig.ignorePackageNameList = new ArrayList<>();
        }
        quickDAOConfig.ignorePackageNameList.add(ignorePackageName);
        return this;
    }

    /**
     * 忽略该实体类
     * @param ignoreClass 需要忽略的实体类
     * */
    public QuickDAO ignoreClass(Class ignoreClass) {
        if (quickDAOConfig.ignoreClassList == null) {
            quickDAOConfig.ignoreClassList = new ArrayList<>();
        }
        quickDAOConfig.ignoreClassList.add(ignoreClass);
        return this;
    }

    /**
     * 过滤实体类
     * @param predicate 过滤实体类函数
     * */
    public QuickDAO filter(Predicate<Class> predicate) {
        quickDAOConfig.predicate = predicate;
        return this;
    }

    /**
     * 是否建立外键约束
     * @param openForeignKey 指定管是否建立外键约束
     * */
    public QuickDAO foreignKey(boolean openForeignKey) {
        quickDAOConfig.openForeignKey = openForeignKey;
        return this;
    }

    /**
     * 是否自动建表
     * @param autoCreateTable 指定是否自动建表
     * */
    public QuickDAO autoCreateTable(boolean autoCreateTable) {
        quickDAOConfig.autoCreateTable = autoCreateTable;
        return this;
    }

    /**
     * 是否自动新增属性
     * @param autoCreateProperty 指定是否自动新增字段
     * */
    public QuickDAO autoCreateProperty(boolean autoCreateProperty) {
        quickDAOConfig.autoCreateProperty = autoCreateProperty;
        return this;
    }

    /**
     * 指定全局Id生成策略
     * @param idStrategy 全局id生成策略
     * */
    public QuickDAO idStrategy(IdStrategy idStrategy) {
        quickDAOConfig.idStrategy = idStrategy;
        return this;
    }

    /**
     * 指定id生成器接口实例
     * <p><b>当id字段策略为IdGenerator起作用</b></p>
     * @param idGenerator id生成器实例
     * */
    public QuickDAO idGenerator(IDGenerator idGenerator) {
        quickDAOConfig.idGenerator = idGenerator;
        return this;
    }

    /**自定义表和列*/
    public TableDefiner define(Class clazz) {
        if(quickDAOConfig.packageNameMap.isEmpty()){
            throw new IllegalArgumentException("请先设置要扫描的实体类包名!");
        }
        if(quickDAOConfig.entityMap.isEmpty()){
            try {
                quickDAOConfig.defaultTableDefiner.getEntityMap();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        quickDAOConfig.defaultTableDefiner.define(clazz);
        return quickDAOConfig.defaultTableDefiner;
    }

    public DAO build(){
        try {
            AbstractTableBuilder tableBuilder = getTableBuilder();
            tableBuilder.autoBuildDatabase();
            tableBuilder.connection.commit();
            tableBuilder.connection.close();
            TableBuilderInvocationHandler invocationHandler = new TableBuilderInvocationHandler(tableBuilder);
            TableBuilder tableBuilderProxy = (TableBuilder) Proxy.newProxyInstance(Thread.currentThread()
                    .getContextClassLoader(), new Class<?>[]{TableBuilder.class},invocationHandler);

            AbstractDAO dao = null;
            if(quickDAOConfig.database instanceof SQLiteDatabase){
                dao = new SQLiteDAO(tableBuilderProxy,quickDAOConfig);
            }else{
                dao = new AbstractDAO(tableBuilderProxy,quickDAOConfig);
            }
            return dao;
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    /**
     * 反向生成实体类
     * @param sourcePath 源文件路径
     * */
    public void reverse(String sourcePath) throws SQLException {
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

        AbstractTableBuilder tableBuilder = getTableBuilder();
        //对比实体类信息与数据库信息
        List<Entity> dbEntityList = tableBuilder.getDatabaseEntity();
        logger.debug("[获取数据库信息]数据库表个数:{}", dbEntityList.size());
        //确定需要新增的表和更新的表
        Collection<Entity> entityList = quickDAOConfig.entityMap.values();
        StringBuilder builder = new StringBuilder();
        String packageName = quickDAOConfig.packageNameMap.keySet().iterator().next();
        for(Entity dbEntity:dbEntityList){
            if(entityList.contains(dbEntity)){
                continue;
            }
            dbEntity.className = StringUtil.Underline2Camel(dbEntity.tableName);
            dbEntity.className = dbEntity.className.toUpperCase().charAt(0)+dbEntity.className.substring(1);
            builder.setLength(0);
            //新建Java类
            builder.append("package "+packageName+";\n");
            builder.append("import cn.schoolwow.quickdao.annotation.Comment;\n\n");
            builder.append("import cn.schoolwow.quickdao.annotation.ColumnType;\n\n");
            builder.append("import cn.schoolwow.quickdao.annotation.ColumnName;\n\n");
            if(null!=dbEntity.comment){
                builder.append("@Comment(\""+dbEntity.comment+"\")\n");
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
                builder.append("\tpublic "+ property.className +" get" +StringUtil.firstLetterUpper(property.name)+"(){\n\t\treturn this."+property.name+";\n\t}\n");
                builder.append("\tpublic void set" +StringUtil.firstLetterUpper(property.name)+"("+property.className+" "+property.name+"){\n\t\tthis."+property.name+"= "+property.name+";\n\t}\n");
            }

            builder.append("};");

            ByteArrayInputStream bais = new ByteArrayInputStream(builder.toString().getBytes());
            Path target = Paths.get(sourcePath+"/"+ packageName.replace(".","/") + "/" + dbEntity.className+".java");
            try {
                Files.createDirectories(target.getParent());
                Files.copy(bais, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private AbstractTableBuilder getTableBuilder(){
        if(null==quickDAOConfig.dataSource){
            throw new IllegalArgumentException("请设置数据库连接池属性!");
        }
        //自动建表
        try {
            Connection connection = quickDAOConfig.dataSource.getConnection();
            connection.setAutoCommit(false);
            String url = connection.getMetaData().getURL();
            logger.info("[数据源地址]{}",url);
            AbstractTableBuilder tableBuilder = null;
            if(url.contains("jdbc:h2")){
                quickDAOConfig.database = new H2Database();
                tableBuilder = new H2TableBuilder(quickDAOConfig);
            }else if(url.contains("jdbc:sqlite")){
                quickDAOConfig.database = new SQLiteDatabase();
                tableBuilder = new SQLiteTableBuilder(quickDAOConfig);
            }else if(url.contains("jdbc:mysql")){
                quickDAOConfig.database = new MySQLDatabase();
                tableBuilder = new MySQLTableBuilder(quickDAOConfig);
            }else if(url.contains("jdbc:postgresql")){
                quickDAOConfig.database = new PostgreDatabase();
                tableBuilder = new PostgreTableBuilder(quickDAOConfig);
            }else if(url.contains("jdbc:sqlserver:")){
                quickDAOConfig.database = new SQLServerDatabase();
                tableBuilder = new SQLServerTableBuilder(quickDAOConfig);
            }else{
                throw new IllegalArgumentException("不支持的数据库类型!");
            }
            tableBuilder.connection = connection;
            if(quickDAOConfig.entityMap.isEmpty()){
                try {
                    quickDAOConfig.defaultTableDefiner.getEntityMap();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            quickDAOConfig.defaultTableDefiner.handleEntityMap();
            return tableBuilder;
        }catch (SQLException e){
            throw new SQLRuntimeException(e);
        }
    }
}
