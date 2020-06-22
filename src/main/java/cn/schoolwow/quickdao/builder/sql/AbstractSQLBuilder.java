package cn.schoolwow.quickdao.builder.sql;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;

public class AbstractSQLBuilder implements SQLBuilder{
    protected Logger logger = LoggerFactory.getLogger(AbstractSQLBuilder.class);
    /**SQL参数占位符*/
    protected static String PLACEHOLDER = "** NOT SPECIFIED **";
    /**SQL语句缓存*/
    protected static ConcurrentHashMap<String,String> sqlCache = new ConcurrentHashMap();
    /**数据库信息对象*/
    public QuickDAOConfig quickDAOConfig;
    /**数据库连接对象*/
    public volatile Connection connection;

    public AbstractSQLBuilder(QuickDAOConfig quickDAOConfig) {
        this.quickDAOConfig = quickDAOConfig;
    }

    @Override
    public PreparedStatement selectByUniqueKey(Object instance) throws Exception {
        String key = "selectByUniqueKey_" + instance.getClass().getName()+"_"+quickDAOConfig.database.getClass().getName();
        Entity entity = quickDAOConfig.entityMap.get(instance.getClass().getName());
        if (!sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            builder.append("select count(1) from " + quickDAOConfig.database.escape(entity.tableName)+" where ");
            for(Property property:entity.uniqueKeyProperties){
                builder.append(quickDAOConfig.database.escape(property.column)+ "=? and ");
            }
            builder.delete(builder.length()-5,builder.length());
            sqlCache.put(key, builder.toString());
        }
        String sql = sqlCache.get(key);
        StringBuilder builder = new StringBuilder(sql.replace("?", PLACEHOLDER));
        PreparedStatement ps = connection.prepareStatement(sql);
        int parameterIndex = 1;
        for(Property property:entity.uniqueKeyProperties){
            setParameter(instance,property,ps,parameterIndex, builder);
            parameterIndex++;
        }
        MDC.put("name","根据唯一性约束查询");
        MDC.put("sql",builder.toString());
        return ps;
    }

    /**
     * 设置参数
     */
    protected static void setParameter(Object parameter, PreparedStatement ps, int parameterIndex, StringBuilder sqlBuilder) throws SQLException {
        ps.setObject(parameterIndex, parameter);
        switch (parameter.getClass().getSimpleName().toLowerCase()) {
            case "boolean": {
                Boolean bool = Boolean.parseBoolean(parameter.toString());
                replaceFirst(sqlBuilder,bool?"1":"0");
            }break;
            case "int": {}
            case "integer":{}
            case "float":{}
            case "long": {}
            case "double": {
                replaceFirst(sqlBuilder,parameter.toString());
            }break;
            case "string": {
                replaceFirst(sqlBuilder,"'"+parameter.toString()+"'");
            }break;
            case "date": {
            }
            default: {
                replaceFirst(sqlBuilder,parameter.toString());
            }
        }
    }

    /**
     * 设置参数
     */
    protected static void setParameter(Object instance, Property property, PreparedStatement ps, int parameterIndex, StringBuilder sqlBuilder) throws Exception{
        Field field = instance.getClass().getDeclaredField(property.name);
        field.setAccessible(true);
        String parameter = null;
        switch (property.simpleTypeName) {
            case "boolean": {
                if (field.getType().isPrimitive()) {
                    ps.setBoolean(parameterIndex, field.getBoolean(instance));
                    parameter = "" + field.getBoolean(instance);
                } else {
                    ps.setObject(parameterIndex, field.get(instance));
                    parameter = "" + field.get(instance);
                }
            }break;
            case "int": {
                ps.setInt(parameterIndex, field.getInt(instance));
                parameter = "" + field.getInt(instance);
            }break;
            case "integer": {
                ps.setObject(parameterIndex, field.get(instance));
                parameter = "" + field.get(instance);
            }break;
            case "float": {
                if (field.getType().isPrimitive()) {
                    ps.setFloat(parameterIndex, field.getFloat(instance));
                    parameter = "" + field.getFloat(instance);
                } else {
                    ps.setObject(parameterIndex, field.get(instance));
                    parameter = "" + field.get(instance);
                }
            }break;
            case "long": {
                if (field.getType().isPrimitive()) {
                    ps.setLong(parameterIndex, field.getLong(instance));
                    parameter = "" + field.getLong(instance);
                } else {
                    ps.setObject(parameterIndex, field.get(instance));
                    parameter = "" + field.get(instance);
                }
            }break;
            case "double": {
                if (field.getType().isPrimitive()) {
                    ps.setDouble(parameterIndex, field.getDouble(instance));
                    parameter = "" + field.getDouble(instance);
                } else {
                    ps.setObject(parameterIndex, field.get(instance));
                    parameter = "" + field.get(instance);
                }
            }break;
            case "string": {
                ps.setString(parameterIndex, field.get(instance) == null ? null : field.get(instance).toString());
                parameter = "'" + (field.get(instance) == null ? "" : field.get(instance).toString()) + "'";
            }break;
            case "date": {
                Object o = field.get(instance);
                if (o == null) {
                    ps.setObject(parameterIndex, null);
                    parameter = "null";
                } else{
                    java.util.Date date = (java.util.Date) o;
                    Timestamp timestamp = new Timestamp(date.getTime());
                    ps.setTimestamp(parameterIndex, timestamp);
                    parameter = "'"+timestamp.toString()+"'";
                }
            }break;
            default: {
                ps.setObject(parameterIndex, field.get(instance));
                parameter = "'" + field.get(instance) + "'";
            }
        }
        replaceFirst(sqlBuilder,parameter);
    }

    /**替换SQL语句的第一个占位符*/
    protected static void replaceFirst(StringBuilder sqlBuilder,String parameter){
        int indexOf = sqlBuilder.indexOf(PLACEHOLDER);
        if (indexOf >= 0) {
            sqlBuilder.replace(indexOf, indexOf + PLACEHOLDER.length(), parameter);
        }
    }
}
