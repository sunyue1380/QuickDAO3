package cn.schoolwow.quickdao.dao.condition.subCondition;

import cn.schoolwow.quickdao.dao.condition.Condition;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.SubQuery;
import cn.schoolwow.quickdao.util.StringUtil;

import java.util.Arrays;
import java.util.List;

public class AbstractSubCondition<T> implements SubCondition<T>{
    private SubQuery subQuery;

    public AbstractSubCondition(SubQuery subQuery) {
        this.subQuery = subQuery;
    }

    @Override
    public SubCondition<T> tableAliasName(String tableAliasName) {
        this.subQuery.tableAliasName = tableAliasName;
        return this;
    }

    @Override
    public SubCondition<T> leftJoin() {
        subQuery.join = "left outer join";
        return this;
    }

    @Override
    public SubCondition<T> rightJoin() {
        subQuery.join = "right outer join";
        return this;
    }

    @Override
    public SubCondition<T> fullJoin() {
        subQuery.join = "full outer join";
        return this;
    }

    @Override
    public SubCondition<T> addNullQuery(String field) {
        subQuery.whereBuilder.append("(" + subQuery.tableAliasName + "." + subQuery.query.quickDAOConfig.database.escape(getColumnNameByFieldName(field)) + " is null) and ");
        return this;
    }

    @Override
    public SubCondition<T> addNotNullQuery(String field) {
        subQuery.whereBuilder.append("(" + subQuery.tableAliasName + "." + subQuery.query.quickDAOConfig.database.escape(getColumnNameByFieldName(field)) + " is not null) and ");
        return this;
    }

    @Override
    public SubCondition<T> addEmptyQuery(String field) {
        subQuery.whereBuilder.append("(" + subQuery.tableAliasName + "." + subQuery.query.quickDAOConfig.database.escape(getColumnNameByFieldName(field)) + " is not null and " + subQuery.tableAliasName + "." + subQuery.query.quickDAOConfig.database.escape(getColumnNameByFieldName(field)) + " = '') and ");
        return this;
    }

    @Override
    public SubCondition<T> addNotEmptyQuery(String field) {
        subQuery.whereBuilder.append("(" + subQuery.tableAliasName + "." + subQuery.query.quickDAOConfig.database.escape(getColumnNameByFieldName(field)) + " is not null and " + subQuery.tableAliasName + "." + subQuery.query.quickDAOConfig.database.escape(getColumnNameByFieldName(field)) + " != '') and ");
        return this;
    }

    @Override
    public SubCondition<T> addInQuery(String field, Object... values) {
        if (null == values || values.length == 0) {
            return this;
        }
        addInQuery(field,values,"in");
        return this;
    }

    @Override
    public SubCondition<T> addInQuery(String field, List values) {
        if (null == values || values.isEmpty()) {
            return this;
        }
        return addInQuery(field, values.toArray(new Object[0]));
    }

    @Override
    public SubCondition<T> addNotInQuery(String field, Object... values) {
        if (null == values || values.length == 0) {
            return this;
        }
        addInQuery(field,values,"not in");
        return this;
    }

    @Override
    public SubCondition<T> addNotInQuery(String field, List values) {
        if (null == values || values.isEmpty()) {
            return this;
        }
        return addNotInQuery(field, values.toArray(new Object[0]));
    }

    @Override
    public SubCondition<T> addBetweenQuery(String field, Object start, Object end) {
        subQuery.whereBuilder.append("("+subQuery.tableAliasName+"." + subQuery.query.quickDAOConfig.database.escape(getColumnNameByFieldName(field)) + " between ? and ? ) and ");
        subQuery.parameterList.add(start);
        subQuery.parameterList.add(end);
        return this;
    }

    @Override
    public SubCondition<T> addLikeQuery(String field, Object value) {
        if (value == null || value.toString().equals("")) {
            return this;
        }
        subQuery.whereBuilder.append("("+subQuery.tableAliasName+"." + subQuery.query.quickDAOConfig.database.escape(getColumnNameByFieldName(field)) + " like ?) and ");
        subQuery.parameterList.add(value);
        return this;
    }

    @Override
    public SubCondition<T> addQuery(String query) {
        subQuery.whereBuilder.append("(" + query + ") and ");
        return this;
    }

    @Override
    public SubCondition<T> addQuery(String field, Object value) {
        addQuery(field, "=", value);
        return this;
    }

    @Override
    public SubCondition<T> addQuery(String field, String operator, Object value) {
        if(null==value){
            addNullQuery(field);
        }else if(value.toString().isEmpty()){
            addEmptyQuery(field);
        }else {
            subQuery.whereBuilder.append("("+subQuery.tableAliasName+"." + subQuery.query.quickDAOConfig.database.escape(getColumnNameByFieldName(field)) + " " + operator + " ?) and ");
            subQuery.parameterList.add(value);
        }
        return this;
    }

    @Override
    public SubCondition<T> joinTable(Class clazz, String primaryField, String joinTableField) {
        return joinTable(clazz,primaryField,joinTableField,subQuery.condition.getUniqueCompositFieldInMainClass(subQuery.entity.clazz,clazz));
    }

    @Override
    public SubCondition<T> joinTable(Class clazz, String primaryField, String joinTableField, String compositField) {
        AbstractSubCondition abstractSubCondition = (AbstractSubCondition) subQuery.condition.joinTable(clazz, primaryField, joinTableField, compositField);
        abstractSubCondition.subQuery.parentSubQuery = this.subQuery;
        abstractSubCondition.subQuery.parentSubCondition = this;
        return abstractSubCondition;
    }

    @Override
    public SubCondition<T> groupBy(String... fields) {
        for(String field:fields){
            subQuery.query.groupByBuilder.append(subQuery.tableAliasName+"." + subQuery.query.quickDAOConfig.database.escape(getColumnNameByFieldName(field)) + ",");
        }
        return this;
    }

    @Override
    public SubCondition<T> orderBy(String... fields) {
        for(String field:fields){
            subQuery.query.orderByBuilder.append(subQuery.tableAliasName + "." + subQuery.query.quickDAOConfig.database.escape(getColumnNameByFieldName(field)) + " asc,");
        }
        return this;
    }

    @Override
    public SubCondition<T> orderByDesc(String... fields) {
        for(String field:fields){
            subQuery.query.orderByBuilder.append(subQuery.tableAliasName + "." + subQuery.query.quickDAOConfig.database.escape(getColumnNameByFieldName(field)) + " desc,");
        }
        return this;
    }

    @Override
    public SubCondition<T> doneSubCondition() {
        if (subQuery.parentSubCondition == null) {
            return this;
        } else {
            return subQuery.parentSubCondition;
        }
    }

    @Override
    public Condition<T> done() {
        return subQuery.condition;
    }

    /**添加in查询*/
    private void addInQuery(String field, Object[] values, String in) {
        subQuery.whereBuilder.append("(" + subQuery.tableAliasName + "." + getColumnNameByFieldName(field) + " " + in + " (");
        for (int i = 0; i < values.length; i++) {
            subQuery.whereBuilder.append("?,");
        }
        subQuery.whereBuilder.deleteCharAt(subQuery.whereBuilder.length() - 1);
        subQuery.whereBuilder.append(") ) and ");
        subQuery.parameterList.addAll(Arrays.asList(values));
    }

    /**
     * 根据字段名查询数据库列名
     * */
    private String getColumnNameByFieldName(String field) {
        if(null==field||field.isEmpty()){
            return field;
        }
        for(Property property:subQuery.entity.properties){
            if(field.equals(property.name)){
                return property.column;
            }
        }
        return field;
    }
}
