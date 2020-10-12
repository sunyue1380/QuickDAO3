package cn.schoolwow.quickdao.builder.sql.dql;

import cn.schoolwow.quickdao.builder.sql.AbstractSQLBuilder;
import cn.schoolwow.quickdao.dao.condition.AbstractCondition;
import cn.schoolwow.quickdao.domain.*;
import org.slf4j.MDC;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AbstractDQLSQLBuilder extends AbstractSQLBuilder implements DQLSQLBuilder{
    public AbstractDQLSQLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public PreparedStatement fetchNull(Class clazz, String field) throws SQLException {
        String key = "fetchNull_" + clazz.getName()+"_"+field+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!sqlCache.containsKey(key)) {
            Entity entity = quickDAOConfig.entityMap.get(clazz.getName());
            StringBuilder builder = new StringBuilder("select ");
            builder.append(columns(entity,"t"));
            builder.append(" from " + entity.escapeTableName + " as t where t." + quickDAOConfig.database.escape(entity.getColumnNameByFieldName(field)) +" is null");
            sqlCache.put(key, builder.toString());
        }
        String sql = sqlCache.get(key);
        PreparedStatement ps = connection.prepareStatement(sql);
        MDC.put("name","Null查询");
        MDC.put("sql",sql);
        return ps;
    }

    @Override
    public PreparedStatement fetch(Class clazz, long id) throws SQLException {
        Entity entity = quickDAOConfig.entityMap.get(clazz.getName());
        return fetch(clazz,entity.id.column,id+"");
    }

    @Override
    public PreparedStatement fetch(Class clazz, String field, Object value) throws SQLException {
        String key = "fetch_" + clazz.getName()+"_"+field+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!sqlCache.containsKey(key)) {
            Entity entity = quickDAOConfig.entityMap.get(clazz.getName());
            StringBuilder builder = new StringBuilder("select ");
            builder.append(columns(entity,"t"));
            Property property = entity.getPropertyByFieldName(field);
            builder.append(" from " + entity.escapeTableName + " as t where t." + quickDAOConfig.database.escape(entity.getColumnNameByFieldName(field)) + " = "+(null==property||null==property.function?"?":property.function)+"");
            sqlCache.put(key, builder.toString());
        }
        String sql = sqlCache.get(key);
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setObject(1,value);
        MDC.put("name","字段查询");
        MDC.put("sql",sql.replace("?",(value instanceof String)?"'"+value.toString()+"'":value.toString()));
        return ps;
    }

    @Override
    public PreparedStatement count(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("select count(1) from " + query.entity.escapeTableName + " as "+query.tableAliasName);
        addJoinTableStatement(query,builder);
        addWhereStatement(query,builder);

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        addMainTableParameters(ps,query,builder);
        addJoinTableParameters(ps,query,builder);
        return ps;
    }

    @Override
    public PreparedStatement insert(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("insert into " + query.entity.escapeTableName + "(");
        builder.append(query.insertBuilder.toString()+") values(");
        for(int i=0;i<query.insertParameterList.size();i++){
            builder.append("?,");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append(")");

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        for (Object parameter : query.insertParameterList) {
            setParameter(parameter,ps,query.parameterIndex++,builder);
        }
        MDC.put("name","插入记录");
        MDC.put("sql",builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement update(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("update " + query.entity.escapeTableName + " as t ");
        addJoinTableStatement(query,builder);
        builder.append(query.setBuilder.toString());
        addWhereStatement(query,builder);
        addJoinTableStatement(query,builder);

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        for (Object parameter : query.updateParameterList) {
            setParameter(parameter,ps,query.parameterIndex++,builder);
        }
        addMainTableParameters(ps,query,builder);
        addJoinTableParameters(ps,query,builder);
        MDC.put("name","批量更新");
        MDC.put("sql",builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement delete(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("delete t from " + query.entity.escapeTableName + " as t");
        addJoinTableStatement(query,builder);
        addWhereStatement(query,builder);

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        addMainTableParameters(ps,query,builder);
        addJoinTableParameters(ps,query,builder);
        MDC.put("name","批量删除");
        MDC.put("sql",builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement getArray(Query query) throws SQLException {
        StringBuilder builder = getArraySQL(query);
        if(!query.unionList.isEmpty()){
            for(AbstractCondition abstractCondition:query.unionList){
                switch(abstractCondition.query.unionType){
                    case Union:{
                        builder.append(" union ");
                    }break;
                    case UnionAll:{
                        builder.append(" union all ");
                    }break;
                }
                builder.append(getArraySQL(abstractCondition.query));
            }
        }
        builder.append(" " + query.orderByBuilder.toString() + " " + query.limit);

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        addMainTableParameters(ps,query,builder);
        addJoinTableParameters(ps,query,builder);
        for (Object parameter : query.havingParameterList) {
            setParameter(parameter,ps,query.parameterIndex++,builder);
        }
        //添加union语句
        for(AbstractCondition abstractCondition:query.unionList){
            for (Object parameter : abstractCondition.query.parameterList) {
                setParameter(parameter,ps,query.parameterIndex++,builder);
            }
            for (SubQuery subQuery : abstractCondition.query.subQueryList) {
                for (Object parameter : subQuery.parameterList) {
                    setParameter(parameter,ps,query.parameterIndex++,builder);
                }
            }
            for(AbstractCondition orCondition:query.orList){
                for (Object parameter : orCondition.query.parameterList) {
                    setParameter(parameter,ps,query.parameterIndex++,builder);
                }
            }
        }
        MDC.put("name","获取列表");
        MDC.put("sql",builder.toString());
        return ps;
    }

    @Override
    public StringBuilder getArraySQL(Query query) {
        StringBuilder builder = new StringBuilder("select " + query.distinct + " ");
        //如果有指定列,则添加指定列
        if(query.columnBuilder.length()>0){
            builder.append(query.columnBuilder.toString());
        }else{
            builder.append(columns(query.entity, query.tableAliasName));
        }
        if(query.compositField){
            for (SubQuery subQuery : query.subQueryList) {
                if(subQuery.columnBuilder.length()==0){
                    builder.append("," + columns(subQuery.entity, subQuery.tableAliasName));
                }
            }
        }
        builder.append(" from " + query.entity.escapeTableName);
        if(null!=query.entity.clazz){
            builder.append(" as " + query.tableAliasName);
        }
        addJoinTableStatement(query,builder);
        addWhereStatement(query,builder);
        builder.append(" " + query.groupByBuilder.toString() + " " + query.havingBuilder.toString());
        return builder;
    }

    private StringBuilder getSubQueryArraySQL(SubQuery subQuery) {
        StringBuilder builder = new StringBuilder("select "+subQuery.columnBuilder.toString());
        builder.append(" from "+quickDAOConfig.database.escape(subQuery.entity.tableName));
        builder.append(" "+ subQuery.whereBuilder.toString());
        return builder;
    }

    /**
     * 添加外键关联查询条件
     */
    private void addJoinTableStatement(Query query,StringBuilder sqlBuilder) {
        for (SubQuery subQuery : query.subQueryList) {
            sqlBuilder.append(" " + subQuery.join + " ");
            if(subQuery.columnBuilder.length()==0){
                sqlBuilder.append(query.quickDAOConfig.database.escape(subQuery.entity.tableName));
            }else{
                sqlBuilder.append("(" + getSubQueryArraySQL(subQuery) + ")");
            }
            sqlBuilder.append(" as " + subQuery.tableAliasName + " on ");
            if (subQuery.parentSubQuery == null) {
                sqlBuilder.append(query.tableAliasName + "." + query.quickDAOConfig.database.escape(subQuery.primaryField) + " = " + subQuery.tableAliasName + "." + query.quickDAOConfig.database.escape(subQuery.joinTableField) + " ");
            }else{
                sqlBuilder.append(subQuery.tableAliasName + "." + query.quickDAOConfig.database.escape(subQuery.joinTableField) + " = " + subQuery.parentSubQuery.tableAliasName + "." + query.quickDAOConfig.database.escape(subQuery.primaryField) + " ");
            }
        }
    }

    /**
     * 添加where的SQL语句
     */
    private void addWhereStatement(Query query,StringBuilder sqlBuilder) {
        //添加查询条件
        sqlBuilder.append(" " + query.whereBuilder.toString());
        for (SubQuery subQuery : query.subQueryList) {
            if (subQuery.columnBuilder.length()==0&&subQuery.whereBuilder.length() > 0) {
                sqlBuilder.append(" and " + subQuery.whereBuilder.toString() + " ");
            }
        }
        for(AbstractCondition orCondition:query.orList){
            sqlBuilder.append(" or (" + orCondition.query.whereBuilder.toString()+")");
        }
    }

    /**
     * 添加主表参数
     */
    protected void addMainTableParameters(PreparedStatement ps, Query query, StringBuilder sqlBuilder) throws SQLException {
        for (SubQuery subQuery : query.subQueryList) {
            if(subQuery.columnBuilder.length()>0){
                for (Object parameter : subQuery.parameterList) {
                    setParameter(parameter,ps,query.parameterIndex++,sqlBuilder);
                }
            }
        }
        for (Object parameter : query.parameterList) {
            setParameter(parameter,ps,query.parameterIndex++,sqlBuilder);
        }
        for(AbstractCondition orCondition:query.orList){
            for (Object parameter : orCondition.query.parameterList) {
                setParameter(parameter,ps,query.parameterIndex++,sqlBuilder);
            }
        }
    }

    /**
     * 添加子表查询参数
     */
    private void addJoinTableParameters(PreparedStatement ps, Query query, StringBuilder sqlBuilder) throws SQLException {
        for (SubQuery subQuery : query.subQueryList) {
            if(subQuery.columnBuilder.length()==0){
                for (Object parameter : subQuery.parameterList) {
                    setParameter(parameter,ps,query.parameterIndex++,sqlBuilder);
                }
            }
        }
    }

    /**
     * 返回列名的SQL语句
     */
    private String columns(Entity entity, String tableAlias) {
        String key = "columns_" + entity.tableName + "_" + tableAlias+quickDAOConfig.database.getClass().getName();
        if (!sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            for (Property property : entity.properties) {
                builder.append(tableAlias + "." + quickDAOConfig.database.escape(property.column) + " as " + tableAlias + "_" + property.column + ",");
            }
            builder.deleteCharAt(builder.length() - 1);
            sqlCache.put(key, builder.toString());
        }
        return sqlCache.get(key);
    }
}
