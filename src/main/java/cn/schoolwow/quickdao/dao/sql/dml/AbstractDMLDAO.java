package cn.schoolwow.quickdao.dao.sql.dml;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.builder.sql.SQLBuilder;
import cn.schoolwow.quickdao.builder.sql.dml.AbstractDMLSQLBuilder;
import cn.schoolwow.quickdao.dao.AbstractDAO;
import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import org.slf4j.MDC;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AbstractDMLDAO extends AbstractSQLDAO implements DMLDAO{
    private AbstractDMLSQLBuilder dmlsqlBuilder;

    public AbstractDMLDAO(SQLBuilder sqlBuilder, AbstractDAO abstractDAO) {
        super(sqlBuilder, abstractDAO);
        this.dmlsqlBuilder = (AbstractDMLSQLBuilder) sqlBuilder;
    }

    @Override
    public int insert(Object instance) {
        if(null==instance){
            return 0;
        }
        int effect = 0;
        try {
            PreparedStatement ps = dmlsqlBuilder.insert(instance);
            effect = ps.executeUpdate();
            Entity entity = dmlsqlBuilder.quickDAOConfig.entityMap.get(instance.getClass().getName());
            if (effect>0&&null!=entity.id&&entity.id.strategy.equals(IdStrategy.AutoIncrement)) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    Field idField = instance.getClass().getDeclaredField(entity.id.name);
                    idField.setAccessible(true);
                    switch(idField.getType().getSimpleName().toLowerCase()){
                        case "int":
                        case "integer":{
                            if(idField.getType().isPrimitive()){
                                idField.setInt(instance,rs.getInt(1));
                            }else{
                                idField.set(instance,Integer.valueOf(rs.getInt(1)));
                            }
                        }break;
                        case "long":{
                            if(idField.getType().isPrimitive()){
                                idField.setLong(instance,rs.getLong(1));
                            }else{
                                idField.set(instance,Long.valueOf(rs.getLong(1)));
                            }
                        }break;
                        case "string":{
                            idField.set(instance,rs.getString(1));
                        }break;
                    }
                }
                rs.close();
            }
            ps.close();
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",effect+"");
        return effect;
    }

    @Override
    public int insert(Object[] instances) {
        if(null==instances||instances.length==0){
            return 0;
        }
        int effect = 0;
        try {
            PreparedStatement ps = dmlsqlBuilder.insert(instances);
            int[] batches = ps.executeBatch();
            for (int batch : batches) {
                effect += batch;
            }
            ps.close();
            dmlsqlBuilder.connection.commit();
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",effect+"");
        return effect;
    }

    @Override
    public int insert(Collection instanceCollection) {
        return insert(instanceCollection.toArray(new Object[0]));
    }

    @Override
    public int update(Object instance) {
        if(null==instance){
            return 0;
        }
        int effect = 0;
        PreparedStatement ps = null;
        Entity entity = dmlsqlBuilder.quickDAOConfig.entityMap.get(instance.getClass().getName());
        try {
            if (entity.uniqueKeyProperties.length>0&&entity.uniqueKeyProperties.length + 1 != entity.properties.length) {
                ps = dmlsqlBuilder.updateByUniqueKey(instance);
                effect = ps.executeUpdate();
            }else if(null!=entity.id){
                ps = dmlsqlBuilder.updateById(instance);
                effect = ps.executeUpdate();
            }else{
                logger.warn("[忽略更新操作]该实例无唯一性约束又无id,忽略该实例的更新操作!");
            }
            if(null!=ps){
                ps.close();
            }
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",effect+"");
        return effect;
    }

    @Override
    public int update(Object[] instances) {
        if(null==instances||instances.length==0){
            return 0;
        }
        int effect = 0;
        PreparedStatement ps = null;
        try {
            Entity entity = dmlsqlBuilder.quickDAOConfig.entityMap.get(instances[0].getClass().getName());
            if(null!=entity.uniqueKeyProperties&&entity.uniqueKeyProperties.length>0&&entity.uniqueKeyProperties.length + 1 != entity.properties.length){
                //根据唯一性约束更新
                ps = dmlsqlBuilder.updateByUniqueKey(instances);
            }else if(null!=entity.id){
                //根据id更新
                ps = dmlsqlBuilder.updateById(instances);
            }
            int[] batches = ps.executeBatch();
            for (int batch : batches) {
                effect += batch;
            }
            ps.close();
            dmlsqlBuilder.connection.commit();
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",effect+"");
        return effect;
    }

    @Override
    public int update(Collection instanceCollection) {
        return update(instanceCollection.toArray(new Object[0]));
    }

    @Override
    public int save(Object instance) {
        if(null==instance){
            return 0;
        }
        if(exist(instance)){
            return update(instance);
        }else{
            return insert(instance);
        }
    }

    @Override
    public int save(Object[] instances) {
        if(null==instances||instances.length==0){
            return 0;
        }
        List insertList = new ArrayList();
        List updateList = new ArrayList();
        int effect = 0;
        for(Object instance:instances){
            if(exist(instance)){
                updateList.add(instance);
            }else{
                insertList.add(instance);
            }
        }
        effect += update(updateList);
        effect += insert(insertList);
        MDC.put("count",effect+"");
        return effect;
    }

    @Override
    public int save(Collection instanceCollection) {
        return save(instanceCollection.toArray(new Object[0]));
    }

    @Override
    public int delete(Class clazz, long id) {
        Entity entity = dmlsqlBuilder.quickDAOConfig.entityMap.get(clazz.getName());
        return delete(clazz,entity.id.column,id);
    }

    @Override
    public int delete(Class clazz, String id) {
        Entity entity = dmlsqlBuilder.quickDAOConfig.entityMap.get(clazz.getName());
        return delete(clazz,entity.id.column,id);
    }

    @Override
    public int delete(Class clazz, String field, Object value) {
        int effect = 0;
        try {
            PreparedStatement ps = dmlsqlBuilder.deleteByProperty(clazz,field,value);
            effect = ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",effect+"");
        return effect;
    }

    @Override
    public int delete(String tableName, String field, Object value) {
        int effect = 0;
        try {
            PreparedStatement ps = dmlsqlBuilder.deleteByProperty(tableName,field,value);
            effect = ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",effect+"");
        return effect;
    }

    @Override
    public int clear(Class clazz) {
        int effect = 0;
        try {
            PreparedStatement ps = dmlsqlBuilder.clear(clazz);
            effect = ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",effect+"");
        return effect;
    }
}
