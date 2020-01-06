package cn.schoolwow.quickdao.dao.sql.dml;

import cn.schoolwow.quickdao.builder.sql.SQLBuilder;
import cn.schoolwow.quickdao.builder.sql.dml.AbstractDMLSQLBuilder;
import cn.schoolwow.quickdao.dao.AbstractDAO;
import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;

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
            if (effect > 0) {
                Property property = dmlsqlBuilder.quickDAOConfig.entityMap.get(instance.getClass().getName()).id;
                if(property.autoIncrement){
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        Field idField = instance.getClass().getDeclaredField(property.name);
                        idField.setAccessible(true);
                        if (idField.getType().isPrimitive()) {
                            idField.setLong(instance, id);
                        } else {
                            idField.set(instance, Long.valueOf(id));
                        }
                    }
                    rs.close();
                }
            }
            ps.close();
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
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
            }else if(hasId(instance)){
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
            List updateByIdList = new ArrayList<>();
            List updateByUniqueKeyList = new ArrayList<>();
            Entity entity = dmlsqlBuilder.quickDAOConfig.entityMap.get(instances[0].getClass().getName());
            for(Object instance:instances){
                if (null!=entity.uniqueKeyProperties&&entity.uniqueKeyProperties.length>0&&entity.uniqueKeyProperties.length + 1 != entity.properties.length) {
                    updateByUniqueKeyList.add(instance);
                }else if(hasId(instance)){
                    updateByIdList.add(instance);
                }
            }
            if(!updateByIdList.isEmpty()){
                ps = dmlsqlBuilder.updateById(updateByIdList.toArray(new Object[0]));
                int[] batches = ps.executeBatch();
                for (int batch : batches) {
                    effect += batch;
                }
                ps.close();
            }
            if(!updateByUniqueKeyList.isEmpty()){
                ps = dmlsqlBuilder.updateByUniqueKey(updateByUniqueKeyList.toArray(new Object[0]));
                int[] batches = ps.executeBatch();
                for (int batch : batches) {
                    effect += batch;
                }
                ps.close();
            }
            dmlsqlBuilder.connection.commit();
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
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
        return effect;
    }
}
