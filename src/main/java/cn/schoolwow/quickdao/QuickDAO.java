package cn.schoolwow.quickdao;

import cn.schoolwow.quickdao.builder.table.*;
import cn.schoolwow.quickdao.dao.AbstractDAO;
import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.database.*;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.handler.DefaultTableDefiner;
import cn.schoolwow.quickdao.handler.TableDefiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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

    /**是否建立外键约束*/
    public QuickDAO foreignKey(boolean openForeignKey) {
        quickDAOConfig.openForeignKey = openForeignKey;
        return this;
    }

    /**是否自动建表*/
    public QuickDAO autoCreateTable(boolean autoCreateTable) {
        quickDAOConfig.autoCreateTable = autoCreateTable;
        return this;
    }

    /**是否自动新增属性*/
    public QuickDAO autoCreateProperty(boolean autoCreateProperty) {
        quickDAOConfig.autoCreateProperty = autoCreateProperty;
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
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        quickDAOConfig.defaultTableDefiner.define(clazz);
        return quickDAOConfig.defaultTableDefiner;
    }

    public DAO build() throws SQLException {
        if (quickDAOConfig.packageNameMap.isEmpty()) {
            throw new IllegalArgumentException("请设置要扫描的实体类包名!");
        }
        if(null==quickDAOConfig.dataSource){
            throw new IllegalArgumentException("请设置数据库连接池属性!");
        }
        if(quickDAOConfig.entityMap.isEmpty()){
            try {
                quickDAOConfig.defaultTableDefiner.getEntityMap();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        quickDAOConfig.defaultTableDefiner.handleEntityMap();
        //自动建表
        Connection connection = quickDAOConfig.dataSource.getConnection();
        connection.setAutoCommit(false);
        String url = connection.getMetaData().getURL();
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
        tableBuilder.autoBuildDatabase();
        tableBuilder.connection.commit();
        tableBuilder.connection.close();

        TableBuilderInvocationHandler invocationHandler = new TableBuilderInvocationHandler(tableBuilder);
        TableBuilder tableBuilderProxy = (TableBuilder) Proxy.newProxyInstance(Thread.currentThread()
                .getContextClassLoader(), new Class<?>[]{TableBuilder.class},invocationHandler);

        AbstractDAO dao = new AbstractDAO(tableBuilderProxy,quickDAOConfig);
        return dao;
    }
}
