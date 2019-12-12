package cn.schoolwow.quickdao.dao.condition;

import cn.schoolwow.quickdao.dao.condition.subCondition.AbstractSubCondition;
import cn.schoolwow.quickdao.dao.condition.subCondition.SQLiteSubCondition;
import cn.schoolwow.quickdao.dao.condition.subCondition.SubCondition;
import cn.schoolwow.quickdao.dao.response.*;
import cn.schoolwow.quickdao.database.H2Database;
import cn.schoolwow.quickdao.database.SQLiteDatabase;
import cn.schoolwow.quickdao.domain.*;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import cn.schoolwow.quickdao.util.StringUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class AbstractCondition<T> implements Condition<T>{
    //转义字符
    public static String[] patterns = new String[]{"%", "_", "[", "[^", "[!", "]"};
    //查询对象
    public Query query;
    //关联表计数
    private int joinTableIndex = 1;

    public AbstractCondition(Query query) {
        this.query = query;
    }

    @Override
    public Condition<T> distinct() {
        query.distinct = "distinct";
        return this;
    }

    @Override
    public Condition<T> addNullQuery(String field) {
        query.whereBuilder.append("("+mainTableAlias+"." + query.quickDAOConfig.database.escape(StringUtil.Camel2Underline(field)) + " is null) and ");
        return this;
    }

    @Override
    public Condition<T> addNotNullQuery(String field) {
        query.whereBuilder.append("("+mainTableAlias+"." + query.quickDAOConfig.database.escape(StringUtil.Camel2Underline(field)) + " is not null) and ");
        return this;
    }

    @Override
    public Condition<T> addEmptyQuery(String field) {
        query.whereBuilder.append("("+mainTableAlias+"." + query.quickDAOConfig.database.escape(StringUtil.Camel2Underline(field)) + " is not null and t." + query.quickDAOConfig.database.escape(StringUtil.Camel2Underline(field)) + " = '') and ");
        return this;
    }

    @Override
    public Condition<T> addNotEmptyQuery(String field) {
        query.whereBuilder.append("("+mainTableAlias+"." + query.quickDAOConfig.database.escape(StringUtil.Camel2Underline(field)) + " is not null and t." + query.quickDAOConfig.database.escape(StringUtil.Camel2Underline(field)) + " != '') and ");
        return this;
    }

    @Override
    public Condition<T> addInQuery(String field, Object[] values) {
        if (values == null || values.length == 0) {
            return this;
        }
        addInQuery(field, values, "in");
        return this;
    }

    @Override
    public Condition<T> addInQuery(String field, List values) {
        if(null==values||values.isEmpty()){
            return this;
        }
        return addInQuery(field,values.toArray(new Object[0]));
    }

    @Override
    public Condition<T> addNotInQuery(String field, Object[] values) {
        if (values == null || values.length == 0) {
            return this;
        }
        addInQuery(field, values, "not in");
        return this;
    }

    @Override
    public Condition<T> addNotInQuery(String field, List values) {
        if(null==values||values.isEmpty()){
            return this;
        }
        return addNotInQuery(field,values.toArray(new Object[0]));
    }

    @Override
    public Condition<T> addBetweenQuery(String field, Object start, Object end) {
        query.whereBuilder.append("("+mainTableAlias+"." + query.quickDAOConfig.database.escape(StringUtil.Camel2Underline(field)) + " between ? and ? ) and ");
        query.parameterList.add(start);
        query.parameterList.add(end);
        return this;
    }

    @Override
    public Condition<T> addLikeQuery(String field, Object value) {
        if (value == null || value.toString().equals("")) {
            return this;
        }
        query.whereBuilder.append("("+mainTableAlias+"." + query.quickDAOConfig.database.escape(StringUtil.Camel2Underline(field)) + " like ?) and ");
        boolean hasContains = false;
        for (String pattern : patterns) {
            if (((String) value).contains(pattern)) {
                query.parameterList.add(value);
                hasContains = true;
                break;
            }
        }
        if (!hasContains) {
            query.parameterList.add("%" + value + "%");
        }
        return this;
    }

    @Override
    public Condition<T> addQuery(String query) {
        this.query.whereBuilder.append("(" + query + ") and ");
        return this;
    }

    @Override
    public Condition<T> addQuery(String field, Object value) {
        addQuery(field, "=", value);
        return this;
    }

    @Override
    public Condition<T> addQuery(String field, String operator, Object value) {
        if(null==value){
            addNullQuery(field);
        }else if(value.toString().isEmpty()){
            addEmptyQuery(field);
        }else {
            query.whereBuilder.append("("+mainTableAlias+"." + query.quickDAOConfig.database.escape(StringUtil.Camel2Underline(field)) + " " + operator + " ?) and ");
            query.parameterList.add(value);
        }
        return this;
    }

    @Override
    public Condition<T> addJSONObjectQuery(JSONObject queryCondition) {
        {
            Property[] properties = query.entity.properties;
            for (Property property : properties) {
                if (queryCondition.containsKey(property.name)) {
                    addQuery(property.name, queryCondition.get(property.name));
                }
                if (queryCondition.containsKey(property.name + "Start")) {
                    addQuery(property.name, ">=", queryCondition.get(property.name + "Start"));
                }
                if (queryCondition.containsKey(property.name + "End")) {
                    addQuery(property.name, "<=", queryCondition.get(property.name + "End"));
                }
                if (queryCondition.containsKey(property.name + "IN")) {
                    addInQuery(property.name, queryCondition.getJSONArray(property.name + "IN"));
                }
                if (queryCondition.containsKey(property.name + "LIKE")) {
                    addLikeQuery(property.name, queryCondition.get(property.name + "LIKE"));
                }
                if (queryCondition.containsKey(property.name + "NOTNULL")) {
                    addNotNullQuery(property.name);
                }
                if (queryCondition.containsKey(property.name + "NULL")) {
                    addNullQuery(property.name);
                }
                if (queryCondition.containsKey(property.name + "NOTEMPTY")) {
                    addNotEmptyQuery(property.name);
                }
            }
            if (queryCondition.containsKey("_orderBy")) {
                if (queryCondition.get("_orderBy") instanceof String) {
                    orderBy(queryCondition.getString("_orderBy"));
                } else if (queryCondition.get("_orderBy") instanceof JSONArray) {
                    JSONArray array = queryCondition.getJSONArray("_orderBy");
                    for (int i = 0; i < array.size(); i++) {
                        orderBy(array.getString(i));
                    }
                }
            }
            if (queryCondition.containsKey("_orderByDesc")) {
                if (queryCondition.get("_orderByDesc") instanceof String) {
                    orderByDesc(queryCondition.getString("_orderByDesc"));
                } else if (queryCondition.get("_orderByDesc") instanceof JSONArray) {
                    JSONArray array = queryCondition.getJSONArray("_orderByDesc");
                    for (int i = 0; i < array.size(); i++) {
                        orderByDesc(array.getString(i));
                    }
                }
            }
            if (queryCondition.containsKey("_pageNumber") && queryCondition.containsKey("_pageSize")) {
                page(queryCondition.getInteger("_pageNumber"), queryCondition.getInteger("_pageSize"));
            }
        }
        //外键关联查询
        {
            JSONArray _joinTables = queryCondition.getJSONArray("_joinTables");
            if (_joinTables == null || _joinTables.size() == 0) {
                return this;
            }
            try {
                Stack<SubCondition> subConditionStack = new Stack<>();
                Stack<JSONArray> joinTablesStack = new Stack<>();
                for (int i = 0; i < _joinTables.size(); i++) {
                    JSONObject _joinTable = _joinTables.getJSONObject(i);
                    String primaryField = _joinTable.getString("_primaryField");
                    String joinTableField = _joinTable.getString("_joinTableField");
                    SubCondition subCondition = joinTable(Class.forName(_joinTable.getString("_class")), primaryField, joinTableField);
                    addSubConditionQuery(subCondition, _joinTable);

                    if (_joinTable.containsKey("_joinTables")) {
                        subConditionStack.push(subCondition);
                        joinTablesStack.push(_joinTable.getJSONArray("_joinTables"));
                        while (!joinTablesStack.isEmpty()) {
                            _joinTables = joinTablesStack.pop();
                            subCondition = subConditionStack.pop();
                            addSubConditionQuery(subCondition, _joinTable);
                            for (int j = 0; j < _joinTables.size(); j++) {
                                _joinTable = _joinTables.getJSONObject(j);
                                primaryField = _joinTable.getString("_primaryField");
                                joinTableField = _joinTable.getString("_joinTableField");
                                SubCondition _subCondition = subCondition.joinTable(Class.forName(_joinTable.getString("_class")), primaryField, joinTableField);
                                if (_joinTable.containsKey("_joinTables")) {
                                    subConditionStack.push(_subCondition);
                                    joinTablesStack.push(_joinTable.getJSONArray("_joinTables"));
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new SQLRuntimeException(e);
            }
        }
        return this;
    }

    @Override
    public Condition<T> union(Condition<T> condition) {
        return union(condition,UnionType.Union);
    }

    @Override
    public Condition<T> union(Condition<T> condition ,UnionType unionType) {
        AbstractCondition abstractCondition = (AbstractCondition) condition;
        abstractCondition.query.unionType = unionType;
        query.unionList.add(abstractCondition);
        return this;
    }

    @Override
    public Condition<T> or() {
        AbstractCondition orCondition = (AbstractCondition) query.dao.query(query.entity.clazz);
        query.orList.add(orCondition);
        return orCondition;
    }

    @Override
    public Condition<T> addUpdate(String field, Object value) {
        query.setBuilder.append(mainTableAlias+"." + query.quickDAOConfig.database.escape(StringUtil.Camel2Underline(field)) + "=?,");
        query.updateParameterList.add(value);
        return this;
    }

    @Override
    public Condition<T> addAggerate(String aggerate, String field) {
        field = StringUtil.Camel2Underline(field);
        String alias = aggerate + "(" + field + ")";
        return addAggerate(aggerate,field,alias);
    }

    @Override
    public Condition<T> addAggerate(String aggerate, String field, String alias) {
        field = StringUtil.Camel2Underline(field);
        query.aggregateColumnBuilder.append(aggerate + "("+mainTableAlias+"." + query.quickDAOConfig.database.escape(field) + ") as " + query.quickDAOConfig.database.escape(alias) + ",");
        return this;
    }

    @Override
    public Condition<T> groupBy(String field) {
        query.groupByBuilder.append(mainTableAlias+"." + query.quickDAOConfig.database.escape(StringUtil.Camel2Underline(field)) + ",");
        return this;
    }

    @Override
    public Condition<T> groupBy(String[] fields) {
        for(String field:fields){
            groupBy(field);
        }
        return this;
    }

    @Override
    public SubCondition joinTable(Class clazz, String primaryField, String joinTableField) {
        return joinTable(clazz,primaryField,joinTableField,getUniqueCompositFieldInMainClass(query.entity.clazz, clazz));
    }

    @Override
    public SubCondition joinTable(Class clazz, String primaryField, String joinTableField, String compositField) {
        SubQuery subQuery = new SubQuery();
        subQuery.entity = query.quickDAOConfig.entityMap.get(clazz.getName());
        subQuery.tableAliasName = mainTableAlias + (joinTableIndex++);
        subQuery.primaryField = StringUtil.Camel2Underline(primaryField);
        subQuery.joinTableField = StringUtil.Camel2Underline(joinTableField);
        subQuery.compositField = StringUtil.Camel2Underline(compositField);
        subQuery.condition = this;
        subQuery.query = query;

        AbstractSubCondition subCondition = null;
        if(query.quickDAOConfig.database instanceof SQLiteDatabase){
            subCondition = new SQLiteSubCondition(subQuery);
        }else{
            subCondition = new AbstractSubCondition(subQuery);
        }
        query.subQueryList.add(subQuery);
        return subCondition;
    }

    @Override
    public Condition<T> orderBy(String field) {
        if(!query.unionList.isEmpty()){
            query.orderByBuilder.append(query.quickDAOConfig.database.escape(mainTableAlias+"_"+StringUtil.Camel2Underline(field))+" asc,");
        }else{
            query.orderByBuilder.append(mainTableAlias+"."+query.quickDAOConfig.database.escape(StringUtil.Camel2Underline(field))+" asc,");
        }
        return this;
    }

    @Override
    public Condition<T> orderByDesc(String field) {
        if(!query.unionList.isEmpty()){
            query.orderByBuilder.append(query.quickDAOConfig.database.escape(mainTableAlias+"_"+StringUtil.Camel2Underline(field))+" desc,");
        }else{
            query.orderByBuilder.append(mainTableAlias+"."+query.quickDAOConfig.database.escape(StringUtil.Camel2Underline(field))+" desc,");
        }
        return this;
    }

    @Override
    public Condition<T> limit(long offset, long limit) {
        query.limit = "limit " + offset + "," + limit;
        return this;
    }

    @Override
    public Condition<T> page(int pageNum, int pageSize) {
        query.limit = "limit " + (pageNum - 1) * pageSize + "," + pageSize;
        query.pageVo = new PageVo<>();
        query.pageVo.setPageSize(pageSize);
        query.pageVo.setCurrentPage(pageNum);
        return this;
    }

    @Override
    public Condition<T> addColumn(String field) {
        field = StringUtil.Camel2Underline(field);
        query.columnBuilder.append(mainTableAlias+"." + query.quickDAOConfig.database.escape(field) + " as " + query.quickDAOConfig.database.escape(mainTableAlias+"_" + field) + ",");
        return this;
    }

    @Override
    public Condition<T> addColumns(String[] fields) {
        for(String field:fields){
            addColumn(field);
        }
        return this;
    }

    @Override
    public Condition<T> excludeColumn(String field) {
        query.excludeColumns.add(field);
        return this;
    }

    @Override
    public Condition<T> excludeColumn(String[] fields) {
        for(String field:fields){
            excludeColumn(field);
        }
        return this;
    }

    @Override
    public Condition<T> addSpecialColumn(String field) {
        query.columnBuilder.append(field+ ",");
        return this;
    }

    @Override
    public Condition<T> addSpecialColumns(String[] fields) {
        for(String field:fields){
            addColumn(field);
        }
        return this;
    }

    @Override
    public Condition<T> compositField() {
        query.compositField = true;
        return this;
    }

    @Override
    public Response<T> execute() {
        if (query.columnBuilder.length() > 0) {
            query.columnBuilder.deleteCharAt(query.columnBuilder.length() - 1);
        }
        if (query.aggregateColumnBuilder.length() > 0) {
            query.aggregateColumnBuilder.deleteCharAt(query.aggregateColumnBuilder.length() - 1);
        }
        if (query.setBuilder.length() > 0) {
            query.setBuilder.deleteCharAt(query.setBuilder.length() - 1);
            query.setBuilder.insert(0, "set ");
        }
        if (query.whereBuilder.length() > 0) {
            query.whereBuilder.delete(query.whereBuilder.length() - 5, query.whereBuilder.length());
            query.whereBuilder.insert(0, "where ");
        }
        if ("group by ".equals(query.groupByBuilder.toString())) {
            query.groupByBuilder.setLength(0);
        } else {
            query.groupByBuilder.deleteCharAt(query.groupByBuilder.length() - 1);
        }
        if (query.orderByBuilder.length() > 0) {
            query.orderByBuilder.deleteCharAt(query.orderByBuilder.length() - 1);
            query.orderByBuilder.insert(0, "order by ");
        }
        //处理所有子查询的where语句
        for (SubQuery subQuery : query.subQueryList) {
            if (subQuery.whereBuilder.length() > 0) {
                subQuery.whereBuilder.delete(subQuery.whereBuilder.length() - 5, subQuery.whereBuilder.length());
            }
        }
        //处理所有union
        for(AbstractCondition condition:query.unionList){
            condition.execute();
        }
        for(AbstractCondition condition:query.orList){
            condition.execute();
            condition.query.whereBuilder.delete(0,5);
        }

        AbstractResponse abstractResponse = null;
        if(query.quickDAOConfig.database instanceof H2Database){
            abstractResponse = new H2Response(query);
        }else{
            abstractResponse = new AbstractResponse(query);
        }
        ResponseInvocationHandler invocationHandler = new ResponseInvocationHandler(abstractResponse);
        return (Response) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class<?>[]{Response.class},invocationHandler);
    }

    /**
     * 找到子表中唯一一个类型为主表的成员变量名
     * @param mainClass 主类
     * @param fieldClass 主类里需要查找的成员变量类型
     */
    public String getUniqueCompositFieldInMainClass(Class mainClass, Class fieldClass) {
        Entity entity = query.quickDAOConfig.entityMap.get(mainClass.getName());
        Field[] fields = entity.compositFields;
        if (fields == null || fields.length == 0) {
            return null;
        }
        int count = 0;
        String fieldName = null;
        for (Field field : fields) {
            if (field.getType().getName().equalsIgnoreCase(fieldClass.getName())) {
                fieldName = field.getName();
                count++;
            }
        }
        if (count == 0) {
            return null;
        } else if (count == 1) {
            return fieldName;
        } else {
            throw new IllegalArgumentException("类[" + mainClass.getName() + "]存在[" + count + "]个类型为[" + fieldClass.getName() + "]的成员变量!请手动指定需要关联的实体类成员变量!");
        }
    }

    /**添加in查询*/
    private void addInQuery(String field, Object[] values, String in) {
        query.whereBuilder.append("(" + mainTableAlias + "." + StringUtil.Camel2Underline(field) + " " + in + " (");
        for (int i = 0; i < values.length; i++) {
            query.whereBuilder.append("?,");
        }
        query.whereBuilder.deleteCharAt(query.whereBuilder.length() - 1);
        query.whereBuilder.append(") ) and ");
        query.parameterList.addAll(Arrays.asList(values));
    }

    /**
     * 添加子查询条件
     */
    private void addSubConditionQuery(SubCondition subCondition, JSONObject _joinTable) {
        Property[] properties = query.quickDAOConfig.entityMap.get(_joinTable.getString("_class")).properties;
        for (Property property : properties) {
            if (_joinTable.containsKey(property.name)) {
                subCondition.addQuery(property.name, _joinTable.get(property.name));
            }
            if (_joinTable.containsKey(property.name + "Start")) {
                subCondition.addQuery(property.name, ">=", _joinTable.get(property.name + "Start"));
            }
            if (_joinTable.containsKey(property.name + "End")) {
                subCondition.addQuery(property.name, "<=", _joinTable.get(property.name + "End"));
            }
            if (_joinTable.containsKey(property.name + "IN")) {
                subCondition.addInQuery(property.name, _joinTable.getJSONArray(property.name + "IN"));
            }
            if (_joinTable.containsKey(property.name + "NOTNULL")) {
                subCondition.addNotNullQuery(property.name);
            }
            if (_joinTable.containsKey(property.name + "NULL")) {
                subCondition.addNullQuery(property.name);
            }
            if (_joinTable.containsKey(property.name + "NOTEMPTY")) {
                subCondition.addNotEmptyQuery(property.name);
            }
        }
        if (_joinTable.containsKey("_orderBy")) {
            if (_joinTable.get("_orderBy") instanceof String) {
                subCondition.orderBy(_joinTable.getString("_orderBy"));
            } else if (_joinTable.get("_orderBy") instanceof JSONArray) {
                JSONArray array = _joinTable.getJSONArray("_orderBy");
                for (int j = 0; j < array.size(); j++) {
                    subCondition.orderBy(array.getString(j));
                }
            }
        }
        if (_joinTable.containsKey("_orderByDesc")) {
            if (_joinTable.get("_orderByDesc") instanceof String) {
                subCondition.orderByDesc(_joinTable.getString("_orderByDesc"));
            } else if (_joinTable.get("_orderByDesc") instanceof JSONArray) {
                JSONArray array = _joinTable.getJSONArray("_orderByDesc");
                for (int j = 0; j < array.size(); j++) {
                    subCondition.orderByDesc(array.getString(j));
                }
            }
        }
        subCondition.done();
    }
}
