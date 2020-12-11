package cn.schoolwow.quickdao.domain;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.builder.table.TableBuilder;
import cn.schoolwow.quickdao.dao.AbstractDAO;
import cn.schoolwow.quickdao.dao.sql.dml.IDGenerator;
import cn.schoolwow.quickdao.dao.sql.dml.SnowflakeIdGenerator;
import cn.schoolwow.quickdao.database.Database;
import cn.schoolwow.quickdao.handler.DefaultTableDefiner;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
     * 待扫描类
     */
    public Map<Class, String> entityClassMap = new HashMap<>();
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
     * 全局Id生成策略
     */
    public IdStrategy idStrategy;
    /**
     * Id生成器实例
     * 默认生成器为雪花算法生成器
     */
    public IDGenerator idGenerator = new SnowflakeIdGenerator();
    /**
     * 当前数据库名称
     */
    public String databaseName;
    /**
     * 全局表编码格式
     * */
    public String charset;
    /**
     * 全局表引擎
     * */
    public String engine;
    /**
     * 扫描后的实体类信息
     * */
    public final Map<String, Entity> entityMap = new HashMap<>();
    /**
     * SQL语句缓存
     * */
    public final ConcurrentHashMap<String,String> sqlCache = new ConcurrentHashMap();
    /**
     * 数据库获取的表信息
     * */
    public List<Entity> dbEntityList;
    /**
     * 虚拟表(dual等)
     * */
    public Entity[] visualTableList;
    /**
     * 数据库信息
     * */
    public Database database;
    /**
     * 扫描实体类信息
     */
    public DefaultTableDefiner defaultTableDefiner;
    /**
     * 建表对象
     */
    public TableBuilder tableBuilder;
    /**
     * 每个数据库对应一个AbstractDAO对象
     * */
    public AbstractDAO abstractDAO;

    /**根据表名获取对应数据库实体类*/
    public Entity getDbEntityByTableName(String tableName){
        for(Entity entity:dbEntityList){
            if(entity.tableName.equals(tableName)){
                return entity;
            }
        }
        return null;
    }
}
