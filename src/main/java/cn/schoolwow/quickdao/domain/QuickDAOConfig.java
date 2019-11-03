package cn.schoolwow.quickdao.domain;

import cn.schoolwow.quickdao.builder.sql.dql.AbstractDQLSQLBuilder;
import cn.schoolwow.quickdao.database.Database;
import cn.schoolwow.quickdao.handler.DefaultTableDefiner;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**数据源访问配置选项*/
public class QuickDAOConfig {
    /**
     * 数据源
     */
    public DataSource dataSource;
    /**
     * 待扫描包名
     */
    public Map<String, String> packageNameMap = new HashMap<>();
    /**
     * 要忽略的类
     */
    public List<Class> ignoreClassList;
    /**
     * 要忽略的包名
     */
    public List<String> ignorePackageNameList;
    /**
     * 函数式接口过滤类
     */
    public Predicate<Class> predicate;
    /**
     * 是否开启外键约束
     */
    public boolean openForeignKey;
    /**
     * 是否启动时自动建表
     */
    public boolean autoCreateTable = true;
    /**
     * 是否自动新增属性
     */
    public boolean autoCreateProperty = true;
    /**
     * 实体表自定义
     */
    public DefaultTableDefiner defaultTableDefiner;
    /**
     * 扫描后的实体类信息
     * */
    public Map<String, Entity> entityMap = new HashMap<>();
    /**
     * 数据库信息
     * */
    public Database database;
    /**
     * 数据库查询构建
     * */
    public AbstractDQLSQLBuilder abstractDQLSQLBuilder;
}
