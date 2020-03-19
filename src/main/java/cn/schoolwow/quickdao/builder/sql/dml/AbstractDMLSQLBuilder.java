package cn.schoolwow.quickdao.builder.sql.dml;

import cn.schoolwow.quickdao.builder.sql.AbstractSQLBuilder;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.util.StringUtil;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class AbstractDMLSQLBuilder extends AbstractSQLBuilder implements DMLSQLBuilder{

    public AbstractDMLSQLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public PreparedStatement insert(Object instance) throws Exception {
        String sql = insert(instance.getClass());
        StringBuilder sqlBuilder = new StringBuilder(sql.replace("?", PLACEHOLDER));
        PreparedStatement ps = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
        insert(ps,instance,sqlBuilder);
        logger.debug("[插入对象]执行SQL:{}",sqlBuilder.toString());
        return ps;
    }

    @Override
    public PreparedStatement insert(Object[] instances) throws Exception {
        String sql = insert(instances[0].getClass());
        connection.setAutoCommit(false);
        PreparedStatement ps = connection.prepareStatement(sql);
        for(Object instance : instances){
            StringBuilder sqlBuilder = new StringBuilder(sql.replace("?", PLACEHOLDER));
            insert(ps,instance,sqlBuilder);
            logger.trace("[批量插入对象]执行SQL:{}",sqlBuilder.toString());
            ps.addBatch();
        }
        return ps;
    }

    @Override
    public PreparedStatement updateByUniqueKey(Object instance) throws Exception{
        String sql = updateByUniqueKey(instance.getClass());
        StringBuilder sqlBuilder = new StringBuilder(sql.replace("?", PLACEHOLDER));
        PreparedStatement ps = connection.prepareStatement(sql);
        updateByUniqueKey(ps,instance,sqlBuilder);
        logger.debug("[更新对象][根据唯一性约束]执行SQL:{}",sqlBuilder.toString());
        return ps;
    }

    @Override
    public PreparedStatement updateByUniqueKey(Object[] instances) throws Exception {
        String sql = updateByUniqueKey(instances[0].getClass());
        connection.setAutoCommit(false);
        PreparedStatement ps = connection.prepareStatement(sql);
        for(Object instance : instances){
            StringBuilder sqlBuilder = new StringBuilder(sql.replace("?", PLACEHOLDER));
            updateByUniqueKey(ps,instance,sqlBuilder);
            logger.trace("[批量更新对象][根据唯一性约束]执行SQL:{}",sqlBuilder.toString());
            ps.addBatch();
        }
        return ps;
    }

    @Override
    public PreparedStatement updateById(Object instance) throws Exception {
        String sql = updateById(instance.getClass());
        StringBuilder sqlBuilder = new StringBuilder(sql.replace("?", PLACEHOLDER));
        PreparedStatement ps = connection.prepareStatement(sql);
        updateById(ps,instance,sqlBuilder);
        logger.debug("[更新对象][根据id更新]执行SQL:{}",sqlBuilder.toString());
        return ps;
    }

    @Override
    public PreparedStatement updateById(Object[] instances) throws Exception {
        String sql = updateById(instances[0].getClass());
        connection.setAutoCommit(false);
        PreparedStatement ps = connection.prepareStatement(sql);
        for(Object instance : instances){
            StringBuilder sqlBuilder = new StringBuilder(sql.replace("?", PLACEHOLDER));
            updateById(ps,instance,sqlBuilder);
            logger.trace("[批量更新对象][根据id更新]执行SQL:{}",sqlBuilder.toString());
            ps.addBatch();
        }
        return ps;
    }

    @Override
    public PreparedStatement deleteByProperty(Class clazz, String property, Object value) throws SQLException {
        String key = "deleteByProperty_" + clazz.getName()+"_"+property+"_"+quickDAOConfig.database.getClass().getName();
        Entity entity = quickDAOConfig.entityMap.get(clazz.getName());
        if (!sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            builder.append("delete from " + quickDAOConfig.database.escape(entity.tableName)+" where "+quickDAOConfig.database.escape(StringUtil.Camel2Underline(property))+" = ?");
            sqlCache.put(key, builder.toString());
        }
        String sql = sqlCache.get(key);
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setObject(1, value);
        logger.debug("[根据字段值删除]执行SQL:{}", sql.replace("?",(value instanceof String)?"'"+value.toString()+"'":value.toString()));
        return ps;
    }

    @Override
    public PreparedStatement clear(Class clazz) throws SQLException {
        String key = "clear_" + clazz.getName()+"_"+quickDAOConfig.database.getClass().getName();
        if (!sqlCache.containsKey(key)) {
            Entity entity = quickDAOConfig.entityMap.get(clazz.getName());
            sqlCache.put(key, "delete from "+quickDAOConfig.database.escape(entity.tableName));
        }
        PreparedStatement preparedStatement = connection.prepareStatement(sqlCache.get(key));
        return preparedStatement;
    }

    /**
     * 获取插入语句
     * @param clazz 实体类对象
     * */
    private String insert(Class clazz){
        String key = "insert_" + clazz.getName()+"_"+quickDAOConfig.database.getClass().getName();
        if (!sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            Entity entity = quickDAOConfig.entityMap.get(clazz.getName());
            builder.append("insert into " + quickDAOConfig.database.escape(entity.tableName) + "(");
            for (Property property : entity.properties) {
                if (property.id&&property.autoIncrement) {
                    continue;
                }
                builder.append(quickDAOConfig.database.escape(property.column) + ",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(") values(");
            for (Property property : entity.properties) {
                if (property.id&&property.autoIncrement) {
                    continue;
                }
                builder.append("?,");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(")");
            sqlCache.put(key, builder.toString());
        }
        return sqlCache.get(key);
    }

    /**
     * 设置插入参数值
     * @param preparedStatement SQL语句
     * @param instance 实例对象
     * @param sqlBuilder sql日志
     * */
    private void insert(PreparedStatement preparedStatement,Object instance, StringBuilder sqlBuilder) throws Exception {
        int parameterIndex = 1;
        Entity entity = quickDAOConfig.entityMap.get(instance.getClass().getName());
        for (Property property : entity.properties) {
            if (property.id&&property.autoIncrement) {
                continue;
            }
            if(property.createdAt||property.updateAt){
                setCurrentDateTime(property,instance);
            }
            setParameter(instance, property, preparedStatement, parameterIndex,sqlBuilder);
            parameterIndex++;
        }
    }

    /**
     * 根据唯一性约束更新语句
     * @param clazz 实例类对象
     * */
    private String updateByUniqueKey(Class clazz){
        String key = "updateByUniqueKey_" + clazz.getName()+"_"+quickDAOConfig.database.getClass().getName();
        if (!sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            Entity entity = quickDAOConfig.entityMap.get(clazz.getName());
            builder.append("update " + quickDAOConfig.database.escape(entity.tableName) + " set ");
            for (Property property : entity.properties) {
                if (property.id || property.unique) {
                    continue;
                }
                if(property.createdAt){
                    continue;
                }
                builder.append(quickDAOConfig.database.escape(property.column) + " = ?,");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(" where ");
            for (Property property : entity.properties) {
                if (property.unique&&!property.id) {
                    builder.append(quickDAOConfig.database.escape(property.column) + "=? and ");
                }
            }
            builder.delete(builder.length() - 5, builder.length());
            sqlCache.put(key, builder.toString());
        }
        return sqlCache.get(key);
    }

    /**
     * 设置根据唯一性约束插入参数值
     * @param preparedStatement SQL语句
     * @param instance 实例对象
     * @param sqlBuilder sql日志
     * */
    private void updateByUniqueKey(PreparedStatement preparedStatement,Object instance, StringBuilder sqlBuilder) throws Exception {
        int parameterIndex = 1;
        Entity entity = quickDAOConfig.entityMap.get(instance.getClass().getName());
        for (Property property : entity.properties) {
            if (property.id || property.unique) {
                continue;
            }
            if(property.createdAt){
                continue;
            }
            if(property.updateAt){
                setCurrentDateTime(property,instance);
            }
            setParameter(instance, property, preparedStatement, parameterIndex,sqlBuilder);
            parameterIndex++;
        }
        for (Property property : entity.properties) {
            if (property.unique&&!property.id) {
                setParameter(instance, property, preparedStatement, parameterIndex,sqlBuilder);
                parameterIndex++;
            }
        }
    }

    /**
     * 根据id更新语句
     * @param clazz 实例类对象
     * */
    private String updateById(Class clazz){
        String key = "updateById_" + clazz.getName()+"_"+quickDAOConfig.database.getClass().getName();
        if (!sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            Entity entity = quickDAOConfig.entityMap.get(clazz.getName());
            builder.append("update " + quickDAOConfig.database.escape(entity.tableName) + " set ");
            for (Property property : entity.properties) {
                if (property.id) {
                    continue;
                }
                if(property.createdAt){
                    continue;
                }
                builder.append(quickDAOConfig.database.escape(property.column) + "=?,");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(" where " + quickDAOConfig.database.escape(entity.id.column) + " = ?");
            sqlCache.put(key, builder.toString());
        }
        return sqlCache.get(key);
    }

    /**
     * 设置根据id更新参数值
     * @param preparedStatement SQL语句
     * @param instance 实例对象
     * @param sqlBuilder sql日志
     * */
    private void updateById(PreparedStatement preparedStatement,Object instance, StringBuilder sqlBuilder) throws Exception {
        int parameterIndex = 1;
        Entity entity = quickDAOConfig.entityMap.get(instance.getClass().getName());
        for (Property property : entity.properties) {
            if (property.id) {
                continue;
            }
            if(property.createdAt){
                continue;
            }
            if(property.updateAt){
                setCurrentDateTime(property,instance);
            }
            setParameter(instance, property, preparedStatement, parameterIndex,sqlBuilder);
            parameterIndex++;
        }
        //再设置id属性
        setParameter(instance, entity.id , preparedStatement, parameterIndex,sqlBuilder);
    }

    /**
     * 设置字段值为当前日期
     * @param property 字段属性
     * @param instance 实例
     * */
    private void setCurrentDateTime(Property property, Object instance) throws Exception {
        Field field = property.entity.clazz.getDeclaredField(property.name);
        field.setAccessible(true);
        switch(property.simpleTypeName){
            case "date":{field.set(instance,field.getType().getConstructor(long.class).newInstance(System.currentTimeMillis()));}break;
            case "timestamp":{field.set(instance,new Timestamp(System.currentTimeMillis()));}break;
            case "calendar":{field.set(instance, Calendar.getInstance());}break;
            case "localdate":{field.set(instance, LocalDate.now());}break;
            case "localdatetime":{field.set(instance, LocalDateTime.now());}break;
            default:{
                logger.warn("[不支持的日期类型]{},目前支持的类型为Date,Calendar,LocalDate,LocalDateTime!",property.simpleTypeName);
            };break;
        }
    }
}
