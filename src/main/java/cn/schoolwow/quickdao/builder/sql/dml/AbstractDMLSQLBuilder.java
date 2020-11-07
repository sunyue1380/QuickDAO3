package cn.schoolwow.quickdao.builder.sql.dml;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.builder.sql.AbstractSQLBuilder;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import org.slf4j.MDC;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;

public class AbstractDMLSQLBuilder extends AbstractSQLBuilder implements DMLSQLBuilder{

    public AbstractDMLSQLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public PreparedStatement insert(Object instance) throws Exception {
        String sql = insert(instance.getClass());
        StringBuilder builder = new StringBuilder(sql.replace("?", PLACEHOLDER));
        PreparedStatement ps = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
        insert(ps,instance, builder);
        MDC.put("name","插入对象");
        MDC.put("sql",builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement insert(Object[] instances) throws Exception {
        String sql = insert(instances[0].getClass());
        connection.setAutoCommit(false);
        PreparedStatement ps = connection.prepareStatement(sql);
        StringBuilder builder = new StringBuilder();
        for(Object instance : instances){
            StringBuilder sqlBuilder = new StringBuilder(sql.replace("?", PLACEHOLDER));
            insert(ps,instance,sqlBuilder);
            builder.append(sqlBuilder.toString()+";");
            ps.addBatch();
        }
        MDC.put("name","批量插入对象");
        MDC.put("sql",builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement updateByUniqueKey(Object instance) throws Exception{
        String sql = updateByUniqueKey(instance.getClass());
        StringBuilder builder = new StringBuilder(sql.replace("?", PLACEHOLDER));
        PreparedStatement ps = connection.prepareStatement(sql);
        updateByUniqueKey(ps,instance, builder);
        MDC.put("name","根据唯一性约束更新对象");
        MDC.put("sql",builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement updateByUniqueKey(Object[] instances) throws Exception {
        String sql = updateByUniqueKey(instances[0].getClass());
        connection.setAutoCommit(false);
        PreparedStatement ps = connection.prepareStatement(sql);
        StringBuilder builder = new StringBuilder();
        for(Object instance : instances){
            StringBuilder sqlBuilder = new StringBuilder(sql.replace("?", PLACEHOLDER));
            updateByUniqueKey(ps,instance,sqlBuilder);
            builder.append(sqlBuilder.toString()+";");
            ps.addBatch();
        }
        MDC.put("name","根据唯一性约束批量更新对象");
        MDC.put("sql",builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement updateById(Object instance) throws Exception {
        String sql = updateById(instance.getClass());
        StringBuilder builder = new StringBuilder(sql.replace("?", PLACEHOLDER));
        PreparedStatement ps = connection.prepareStatement(sql);
        updateById(ps,instance, builder);
        MDC.put("name","根据ID更新对象");
        MDC.put("sql",builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement updateById(Object[] instances) throws Exception {
        String sql = updateById(instances[0].getClass());
        connection.setAutoCommit(false);
        PreparedStatement ps = connection.prepareStatement(sql);
        StringBuilder builder = new StringBuilder();
        for(Object instance : instances){
            StringBuilder sqlBuilder = new StringBuilder(sql.replace("?", PLACEHOLDER));
            updateById(ps,instance,sqlBuilder);
            builder.append(sqlBuilder.toString()+";");
            ps.addBatch();
        }
        MDC.put("name","根据ID批量更新对象");
        MDC.put("sql",builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement deleteByProperty(Class clazz, String property, Object value) throws SQLException {
        String key = "deleteByProperty_" + clazz.getName()+"_"+property+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!sqlCache.containsKey(key)) {
            Entity entity = quickDAOConfig.entityMap.get(clazz.getName());
            StringBuilder builder = new StringBuilder();
            builder.append("delete from " + entity.escapeTableName + " where " + quickDAOConfig.database.escape(entity.getColumnNameByFieldName(property)) + " = ?");
            sqlCache.put(key, builder.toString());
        }
        String sql = sqlCache.get(key);
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setObject(1, value);
        MDC.put("name","根据单个字段删除");
        MDC.put("sql",sql.replace("?",(value instanceof String)?"'"+value.toString()+"'":value.toString()));
        return ps;
    }

    @Override
    public PreparedStatement deleteByProperty(String tableName, String property, Object value) throws SQLException {
        String sql = "delete from " + quickDAOConfig.database.escape(tableName) + " where " + quickDAOConfig.database.escape(property) + " = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setObject(1, value);
        MDC.put("name","根据单个字段删除");
        MDC.put("sql",sql.replace("?",(value instanceof String)?"'"+value.toString()+"'":value.toString()));
        return ps;
    }

    @Override
    public PreparedStatement clear(Class clazz) throws SQLException {
        String key = "clear_" + clazz.getName()+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!sqlCache.containsKey(key)) {
            Entity entity = quickDAOConfig.entityMap.get(clazz.getName());
            sqlCache.put(key, "delete from "+entity.escapeTableName);
        }
        PreparedStatement preparedStatement = connection.prepareStatement(sqlCache.get(key));
        return preparedStatement;
    }

    /**
     * 获取插入语句
     * @param clazz 实体类对象
     * */
    private String insert(Class clazz){
        String key = "insert_" + clazz.getName()+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            Entity entity = quickDAOConfig.entityMap.get(clazz.getName());
            builder.append("insert into " + entity.escapeTableName + "(");
            for (Property property : entity.properties) {
                if (property.id&&property.strategy==IdStrategy.AutoIncrement) {
                    continue;
                }
                builder.append(quickDAOConfig.database.escape(property.column) + ",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(") values(");
            for (Property property : entity.properties) {
                if (property.id&&property.strategy==IdStrategy.AutoIncrement) {
                    continue;
                }
                builder.append((null==property.function?"?":property.function)+",");
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
            if (property.id&&property.strategy==IdStrategy.AutoIncrement) {
                continue;
            }
            if(property.id&&property.strategy==IdStrategy.IdGenerator){
                Field idField = instance.getClass().getDeclaredField(property.name);
                idField.setAccessible(true);
                long value = quickDAOConfig.idGenerator.getNextId();
                switch(idField.getType().getSimpleName().toLowerCase()){
                    case "int":
                    case "integer":{
                        if(idField.getType().isPrimitive()){
                            idField.setInt(instance, (int) value);
                        }else{
                            idField.set(instance,Integer.valueOf(value+""));
                        }
                    }break;
                    case "long":{
                        if(idField.getType().isPrimitive()){
                            idField.setLong(instance,value);
                        }else{
                            idField.set(instance,Long.valueOf(value));
                        }
                    }break;
                    case "string":{
                        idField.set(instance,value+"");
                    }break;
                }
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
        String key = "updateByUniqueKey_" + clazz.getName()+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            Entity entity = quickDAOConfig.entityMap.get(clazz.getName());
            builder.append("update " + entity.escapeTableName + " set ");
            for (Property property : entity.properties) {
                if (property.id || property.unique) {
                    continue;
                }
                if(property.createdAt){
                    continue;
                }
                builder.append(quickDAOConfig.database.escape(property.column) + " = "+(null==property.function?"?":property.function)+",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(" where ");
            for (Property property : entity.properties) {
                if (property.unique&&!property.id) {
                    builder.append(quickDAOConfig.database.escape(property.column) + " = ? and ");
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
        String key = "updateById_" + clazz.getName()+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            Entity entity = quickDAOConfig.entityMap.get(clazz.getName());
            builder.append("update " + entity.escapeTableName + " set ");
            for (Property property : entity.properties) {
                if (property.id) {
                    continue;
                }
                if(property.createdAt){
                    continue;
                }
                builder.append(quickDAOConfig.database.escape(property.column) + " = ?,");
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
