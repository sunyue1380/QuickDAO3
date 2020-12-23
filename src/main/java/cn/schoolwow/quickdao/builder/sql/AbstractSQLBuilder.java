package cn.schoolwow.quickdao.builder.sql;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.reflect.Field;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AbstractSQLBuilder implements SQLBuilder{
    protected final static Logger logger = LoggerFactory.getLogger(AbstractSQLBuilder.class);
    /*格式化旧版本的Date类型**/
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    /**格式化日期参数*/
    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");
    /**格式化日期参数*/
    private final static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**SQL参数占位符*/
    protected final static String PLACEHOLDER = "** NOT SPECIFIED **";
    /**数据库信息对象*/
    public QuickDAOConfig quickDAOConfig;
    /**数据库连接对象*/
    public volatile Connection connection;

    public AbstractSQLBuilder(QuickDAOConfig quickDAOConfig) {
        this.quickDAOConfig = quickDAOConfig;
    }

    @Override
    public PreparedStatement selectCountById(Object instance) throws Exception {
        String key = "selectCountById_" + instance.getClass().getName()+"_"+quickDAOConfig.database.getClass().getName();
        Entity entity = quickDAOConfig.entityMap.get(instance.getClass().getName());
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            builder.append("select count(1) from " + entity.escapeTableName + " where ");
            builder.append(entity.id.column+" = "+(null==entity.id.function?"?":entity.id.function)+" ");
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }
        String sql = quickDAOConfig.sqlCache.get(key);
        PreparedStatement ps = connection.prepareStatement(sql);
        Field field = instance.getClass().getDeclaredField(entity.id.name);
        field.setAccessible(true);
        Object value = field.get(instance);
        ps.setObject(1,value);
        MDC.put("name","根据id查询");
        MDC.put("sql",sql.replace("?",value==null?"":value.toString()));
        return ps;
    }

    @Override
    public PreparedStatement selectCountByUniqueKey(Object instance) throws Exception {
        String key = "selectCountByUniqueKey_" + instance.getClass().getName()+"_"+quickDAOConfig.database.getClass().getName();
        Entity entity = quickDAOConfig.entityMap.get(instance.getClass().getName());
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            builder.append("select count(1) from " + entity.escapeTableName + " where ");
            for(Property property:entity.uniqueKeyProperties){
                builder.append(quickDAOConfig.database.escape(property.column)+ "= "+(null==property.function?"?":property.function)+" and ");
            }
            builder.delete(builder.length()-5,builder.length());
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }
        String sql = quickDAOConfig.sqlCache.get(key);
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
     * DQL查询操作设置参数
     */
    protected static void setParameter(Object parameter, PreparedStatement ps, int parameterIndex, StringBuilder sqlBuilder) throws SQLException {
        if(null==parameter){
            ps.setObject(parameterIndex,null);
            replaceFirst(sqlBuilder,"null");
            return;
        }
        switch (parameter.getClass().getSimpleName().toLowerCase()) {
            case "boolean": {
                ps.setBoolean(parameterIndex, (Boolean) parameter);
            }break;
            case "int": {}
            case "integer":{
                ps.setInt(parameterIndex, (Integer) parameter);
            };break;
            case "float":{
                ps.setFloat(parameterIndex, (Float) parameter);
            };break;
            case "long": {
                ps.setLong(parameterIndex, (Long) parameter);
            };break;
            case "double": {
                ps.setDouble(parameterIndex, (Double) parameter);
            }break;
            case "string": {
                ps.setString(parameterIndex, (String) parameter);
            }break;
            case "date": {
                if(parameter instanceof Date){
                    ps.setDate(parameterIndex, (Date) parameter);
                }else{
                    java.util.Date d = (java.util.Date) parameter;
                    ps.setDate(parameterIndex, new Date(d.getTime()));
                }
            };break;
            case "timestamp": {
                ps.setTimestamp(parameterIndex, (Timestamp) parameter);
            }break;
            default:{
                ps.setObject(parameterIndex,parameter);
            }
        }
        if(!logger.isDebugEnabled()){
            return;
        }
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
                java.util.Date date = (java.util.Date) parameter;
                LocalDate localDate = LocalDate.of(date.getYear()+1900,date.getMonth(),date.getDay());
                replaceFirst(sqlBuilder,"'"+dateFormatter.format(localDate)+"'");
            }break;
            case "timestamp": {
                java.util.Date date = (java.util.Date) parameter;
                LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                replaceFirst(sqlBuilder,"'"+dateTimeFormatter.format(localDateTime)+"'");
            }break;
            case "localdate": {
                LocalDate localDate = (LocalDate) parameter;
                replaceFirst(sqlBuilder,"'"+dateFormatter.format(localDate)+"'");
            }break;
            case "localdatetime": {
                LocalDateTime localDateTime = (LocalDateTime) parameter;
                replaceFirst(sqlBuilder,"'"+dateTimeFormatter.format(localDateTime)+"'");
            }break;

            default: {
                replaceFirst(sqlBuilder,parameter.toString());
            }
        }
    }

    /**
     * DML操作设置参数
     */
    protected static void setParameter(Object instance, Property property, PreparedStatement ps, int parameterIndex, StringBuilder sqlBuilder) throws Exception{
        Class tempClass = instance.getClass();
        Field field = null;
        while(null==field&&null!=tempClass){
            Field[] fields = tempClass.getDeclaredFields();
            for(Field field1:fields){
                if(field1.getName().equals(property.name)){
                    field = field1;
                    break;
                }
            }
            tempClass = tempClass.getSuperclass();
        }
        if(null==field){
            throw new IllegalArgumentException("字段不存在!字段名:"+property.name+",类名:"+instance.getClass().getName());
        }
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
                if (null==o) {
                    ps.setObject(parameterIndex, null);
                    parameter = "null";
                } else{
                    java.util.Date date = (java.util.Date) o;
                    ps.setTimestamp(parameterIndex, new Timestamp(date.getTime()));
                    synchronized (simpleDateFormat){
                        parameter = "'"+simpleDateFormat.format(date)+"'";
                    }
                }
            };break;
            case "timestamp": {
                Object o = field.get(instance);
                if (null==o) {
                    ps.setObject(parameterIndex, null);
                    parameter = "null";
                } else{
                    java.util.Date date = (java.util.Date) o;
                    ps.setTimestamp(parameterIndex, new Timestamp(date.getTime()));
                    synchronized (simpleDateFormat){
                        parameter = "'"+simpleDateFormat.format(date)+"'";
                    }
                }
            }break;
            case "localdate": {
                Object o = field.get(instance);
                if(null==o){
                    ps.setObject(parameterIndex, null);
                    parameter = "null";
                }else{
                    ps.setObject(parameterIndex, o);
                    LocalDate localDate = (LocalDate) o;
                    parameter = "'"+dateFormatter.format(localDate)+"'";
                }
            }break;
            case "localdatetime": {
                Object o = field.get(instance);
                if(null==o){
                    ps.setObject(parameterIndex, null);
                    parameter = "null";
                }else{
                    ps.setObject(parameterIndex, o);
                    LocalDateTime localDate = (LocalDateTime) o;
                    parameter = "'"+dateTimeFormatter.format(localDate)+"'";
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
