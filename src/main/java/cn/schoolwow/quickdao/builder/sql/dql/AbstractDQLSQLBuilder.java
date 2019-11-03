package cn.schoolwow.quickdao.builder.sql.dql;

import cn.schoolwow.quickdao.builder.sql.AbstractSQLBuilder;
import cn.schoolwow.quickdao.dao.condition.AbstractCondition;
import cn.schoolwow.quickdao.dao.condition.Condition;
import cn.schoolwow.quickdao.domain.*;
import cn.schoolwow.quickdao.util.StringUtil;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AbstractDQLSQLBuilder extends AbstractSQLBuilder implements DQLSQLBuilder{
    public AbstractDQLSQLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public PreparedStatement fetchNull(Class clazz, String field) throws SQLException {
        String key = "fetchNull_" + clazz.getName()+"_"+field+"_"+quickDAOConfig.database.getClass().getName();
        Entity entity = quickDAOConfig.entityMap.get(clazz.getName());
        if (!sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder("select ");
            builder.append(columns(entity,"t"));
            builder.append(" from " + quickDAOConfig.database.escape(entity.tableName)+" as t where "+ StringUtil.Camel2Underline(field) +" is null");
            sqlCache.put(key, builder.toString());
        }
        String sql = sqlCache.get(key);
        PreparedStatement ps = connection.prepareStatement(sql);
        logger.debug("[Null查询]执行SQL:{}",sql);
        return ps;
    }

    @Override
    public PreparedStatement fetch(Class clazz, long id) throws SQLException {
        Entity entity = quickDAOConfig.entityMap.get(clazz.getName());
        return fetch(clazz,entity.id.column,id+"");
    }

    @Override
    public PreparedStatement fetch(Class clazz, String field, Object value) throws SQLException {
        String key = "fetch_" + clazz.getName()+"_"+field+"_"+quickDAOConfig.database.getClass().getName();
        Entity entity = quickDAOConfig.entityMap.get(clazz.getName());
        if (!sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder("select ");
            builder.append(columns(entity,"t"));
            builder.append(" from " + quickDAOConfig.database.escape(entity.tableName)+" as t where "+ StringUtil.Camel2Underline(field) +" = ?");
            sqlCache.put(key, builder.toString());
        }
        String sql = sqlCache.get(key);
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setObject(1,value);
        logger.debug("[根据字段查询]执行SQL:{}",sql.replace("?",value.toString()));
        return ps;
    }

    @Override
    public PreparedStatement count(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("select count(1) from "+query.quickDAOConfig.database.escape(query.entity.tableName)+" as t");
        addJoinTableStatement(query,builder);
        addWhereStatement(query,builder);

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        addMainTableParameters(ps,query,builder);
        addJoinTableParameters(ps,query,builder);
        logger.trace("[获取总数]执行SQL:{}", builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement update(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("update "+query.quickDAOConfig.database.escape(query.entity.tableName)+" as t ");
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
        logger.debug("[批量更新]执行SQL:{}", builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement delete(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("delete t from "+query.quickDAOConfig.database.escape(query.entity.tableName)+" as t");
        addJoinTableStatement(query,builder);
        addWhereStatement(query,builder);

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        addMainTableParameters(ps,query,builder);
        addJoinTableParameters(ps,query,builder);
        logger.debug("[批量删除]执行SQL:{}", builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement getArray(Query query) throws SQLException {
        StringBuilder builder = getArraySQL(query);

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        addMainTableParameters(ps,query,builder);
        addJoinTableParameters(ps,query,builder);
        logger.debug("[获取列表]执行SQL:{}", builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement getAggerateList(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("select "+ query.distinct);
        if (query.columnBuilder.length() > 0) {
            builder.append(" "+query.columnBuilder.toString() + ",");
        }
        builder.append(query.aggregateColumnBuilder.toString() + " from " + quickDAOConfig.database.escape(query.entity.tableName) + " as "+Condition.mainTableAlias);
        addJoinTableStatement(query,builder);
        addWhereStatement(query,builder);
        builder.append(" " + query.groupByBuilder.toString() + " " + query.orderByBuilder.toString().replace(Condition.mainTableAlias+".","") + " " + query.limit);

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        addMainTableParameters(ps,query,builder);
        addJoinTableParameters(ps,query,builder);
        logger.debug("[获取聚合列表]执行SQL:{}", builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement getValueList(String column, Query query) throws SQLException {
        column = StringUtil.Camel2Underline(column);
        StringBuilder builder = new StringBuilder("select "+query.distinct);
        builder.append(" "+Condition.mainTableAlias+"."+quickDAOConfig.database.escape(column)+" as "+Condition.mainTableAlias+"_"+column);
        builder.append(" from "+quickDAOConfig.database.escape(query.entity.tableName)+" as "+Condition.mainTableAlias);
        addJoinTableStatement(query,builder);
        addWhereStatement(query,builder);
        builder.append(" " + query.orderByBuilder.toString() + " " + query.limit);

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        addMainTableParameters(ps,query,builder);
        addJoinTableParameters(ps,query,builder);
        logger.debug("[获取单列列表]执行SQL:{}", builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement getPartList(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("select "+query.distinct+" ");
        if(!query.excludeColumns.isEmpty()){
            for(Property property:query.entity.properties){
                if(query.excludeColumns.contains(property.name)){
                    continue;
                }
                builder.append(Condition.mainTableAlias+"."+query.quickDAOConfig.database.escape(property.column)+" as "+query.quickDAOConfig.database.escape(Condition.mainTableAlias+"_" + property.column)+",");
            }
            builder.deleteCharAt(builder.length()-1);
        }else{
            builder.append(query.columnBuilder.toString());
        }
        builder.append(" from "+quickDAOConfig.database.escape(query.entity.tableName)+" as "+Condition.mainTableAlias+" ");
        addJoinTableStatement(query,builder);
        addWhereStatement(query,builder);
        builder.append(" " + query.orderByBuilder.toString() + " " + query.limit);

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        addMainTableParameters(ps,query,builder);
        addJoinTableParameters(ps,query,builder);
        logger.debug("[获取部分字段列表]执行SQL:{}", builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement getUnionList(Query query) throws SQLException {
        if(query.unionList.isEmpty()){
            throw new IllegalArgumentException("请先调用union()方法!");
        }
        StringBuilder builder = new StringBuilder(getArraySQL(query));
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
        builder.append(" " + query.orderByBuilder.toString() + " " + query.limit);

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));

        //设置参数值
        {
            Query[] queries = new Query[query.unionList.size()+1];
            queries[0] = query;
            for(int i=1;i<queries.length;i++){
                queries[i] = query.unionList.get(i-1).query;
            }
            for(Query _query:queries){
                for (Object parameter : _query.parameterList) {
                    setParameter(parameter,ps,query.parameterIndex++,builder);
                }
                for (SubQuery subQuery : _query.subQueryList) {
                    for (Object parameter : subQuery.parameterList) {
                        setParameter(parameter,ps,query.parameterIndex++,builder);
                    }
                }
            }
        }
        logger.debug("[获取联合查询列表]执行SQL:{}", builder.toString());
        return ps;
    }

    private StringBuilder getArraySQL(Query query) {
        StringBuilder builder = new StringBuilder("select " + query.distinct + " ");
        builder.append(columns(query.entity, Condition.mainTableAlias));
        if(query.compositField){
            for (SubQuery subQuery : query.subQueryList) {
                builder.append("," + columns(subQuery.entity, subQuery.tableAliasName));
            }
        }
        builder.append(" from "+quickDAOConfig.database.escape(query.entity.tableName)+" as "+Condition.mainTableAlias+" ");
        addJoinTableStatement(query,builder);
        addWhereStatement(query,builder);
        return builder;
    }

    /**
     * 添加外键关联查询条件
     */
    protected void addJoinTableStatement(Query query,StringBuilder sqlBuilder) {
        for (SubQuery subQuery : query.subQueryList) {
            if (subQuery.parentSubQuery == null) {
                //如果parentSubCondition为空,则为主表关联子表
                sqlBuilder.append(" "+subQuery.join + " " + query.quickDAOConfig.database.escape(subQuery.entity.tableName) + " as " + subQuery.tableAliasName + " on t." + query.quickDAOConfig.database.escape(subQuery.primaryField) + " = " + subQuery.tableAliasName + "." + query.quickDAOConfig.database.escape(subQuery.joinTableField)+" ");
            } else {
                //如果parentSubCondition不为空,则为子表关联子表
                sqlBuilder.append(" "+subQuery.join + " " + query.quickDAOConfig.database.escape(subQuery.entity.tableName) + " as " + subQuery.tableAliasName + " on " + subQuery.tableAliasName + "." + query.quickDAOConfig.database.escape(subQuery.joinTableField) + " = " + subQuery.parentSubQuery.tableAliasName + "." + query.quickDAOConfig.database.escape(subQuery.primaryField) + " ");
            }
        }
    }

    /**
     * 添加where的SQL语句
     */
    protected void addWhereStatement(Query query,StringBuilder sqlBuilder) {
        //添加查询条件
        sqlBuilder.append(" " + query.whereBuilder.toString());
        for (SubQuery subQuery : query.subQueryList) {
            if (subQuery.whereBuilder.length() > 0) {
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
    protected void addJoinTableParameters(PreparedStatement ps, Query query, StringBuilder sqlBuilder) throws SQLException {
        for (SubQuery subQuery : query.subQueryList) {
            for (Object parameter : subQuery.parameterList) {
                setParameter(parameter,ps,query.parameterIndex++,sqlBuilder);
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
