package cn.schoolwow.quickdao.dao.response;

import cn.schoolwow.quickdao.dao.condition.AbstractCondition;
import cn.schoolwow.quickdao.dao.condition.Condition;
import cn.schoolwow.quickdao.domain.*;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import cn.schoolwow.quickdao.util.StringUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AbstractResponse<T> implements Response<T>{
    private Logger logger = LoggerFactory.getLogger(AbstractResponse.class);
    //查询对象参数
    public Query query;
    //数据库连接
    public Connection connection;

    public AbstractResponse(Query query) {
        this.query = query;
    }

    @Override
    public long count() {
        long count = 0;
        query.parameterIndex = 1;
        try {
            PreparedStatement ps = query.dqlsqlBuilder.count(query);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                count = resultSet.getLong(1);
            }
            resultSet.close();
            ps.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        query.parameterIndex = 1;
        return count;
    }

    @Override
    public long update() {
        long count = 0;
        try {
            PreparedStatement ps = query.dqlsqlBuilder.update(query);
            count= ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return count;
    }

    @Override
    public long delete() {
        long count = 0;
        try {
            PreparedStatement ps = query.dqlsqlBuilder.delete(query);
            count= ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return count;
    }

    @Override
    public T getOne() {
        List<T> list = getList();
        if (list == null || list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public List getList() {
        return getArray().toJavaList(query.entity.clazz);
    }

    @Override
    public JSONArray getArray() {
        JSONArray array = null;
        try {
            PreparedStatement ps = query.dqlsqlBuilder.getArray(query);
            array = new JSONArray(count(query));
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                JSONObject o = getObject(query.entity, AbstractCondition.mainTableAlias, resultSet);
                if(query.compositField){
                    mappingCompositResultSetToJSONArray(resultSet,o);
                }
                array.add(o);
            }
            resultSet.close();
            ps.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return array;
    }

    @Override
    public List getValueList(Class clazz, String column) {
        try {
            PreparedStatement ps = query.dqlsqlBuilder.getValueList(column,query);
            JSONArray array = new JSONArray(count(query));
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                array.add(resultSet.getString(1));
            }
            resultSet.close();
            ps.close();
            return array.toJavaList(clazz);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public JSONArray getAggerateList() {
        try {
            PreparedStatement ps = query.dqlsqlBuilder.getAggerateList(query);
            JSONArray array = new JSONArray(count(query));
            ResultSet resultSet = ps.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                JSONObject o = new JSONObject();
                for (int i = 1; i <= columnCount; i++) {
                    o.put(metaData.getColumnName(i), resultSet.getString(i));
                }
                array.add(o);
            }
            resultSet.close();
            ps.close();
            return array;
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public PageVo getAggeratePagingList() {
        query.pageVo.setList(getAggerateList());
        setPageVo();
        return query.pageVo;
    }

    @Override
    public <E> PageVo<E> getAggeratePagingList(Class<E> clazz) {
        query.pageVo.setList(getAggerateList().toJavaList(clazz));
        setPageVo();
        return query.pageVo;
    }

    @Override
    public List getPartList() {
        JSONArray array = null;
        try {
            PreparedStatement ps = query.dqlsqlBuilder.getPartList(query);
            ResultSet resultSet = ps.executeQuery();
            array = mappingResultSetToJSONArray(resultSet);
            resultSet.close();
            ps.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return array.toJavaList(query.entity.clazz);
    }

    @Override
    public JSONArray getSpecialList() {
        JSONArray array = null;
        try {
            PreparedStatement ps = query.dqlsqlBuilder.getPartList(query);
            ResultSet resultSet = ps.executeQuery();
            array = mappingResultSetToJSONArray(resultSet);
            resultSet.close();
            ps.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return array;
    }

    @Override
    public PageVo<T> getPagingList() {
        query.pageVo.setList(getList());
        setPageVo();
        return query.pageVo;
    }

    @Override
    public PageVo<T> getPartPagingList() {
        query.pageVo.setList(getPartList());
        setPageVo();
        return query.pageVo;
    }

    @Override
    public List getUnionList() {
        JSONArray array = null;
        try {
            PreparedStatement ps = query.dqlsqlBuilder.getUnionList(query);
            ResultSet resultSet = ps.executeQuery();
            array = mappingResultSetToJSONArray(resultSet);
            resultSet.close();
            ps.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return array.toJavaList(query.entity.clazz);
    }

    @Override
    public PageVo<T> getUnionPagingList() {
        query.pageVo.setList(getUnionList());
        setPageVo();
        return query.pageVo;
    }

    private int count(Query query) throws SQLException {
        String replaceString = " " + query.orderByBuilder.toString() + " " + query.limit;
        if(!replaceString.trim().isEmpty()){
            query.sql = query.sql.replace(replaceString,"");
        }
        String countSQL = "select count(1) from ("+query.sql+") as foo";
        logger.debug("[分页总数]执行SQL:{}",countSQL);
        PreparedStatement ps = connection.prepareStatement(countSQL);
        ResultSet resultSet = ps.executeQuery();
        int count = 0;
        if(resultSet.next()){
            count = resultSet.getInt(1);
        }
        resultSet.close();
        return count;
    }

    /**设置分页对象*/
    private void setPageVo() {
        if (query.pageVo == null) {
            throw new IllegalArgumentException("请先调用page()函数!");
        }
        try {
            query.pageVo.setTotalSize(count(query));
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        query.pageVo.setTotalPage((int)(query.pageVo.getTotalSize() / query.pageVo.getPageSize() + (query.pageVo.getTotalSize() % query.pageVo.getPageSize() > 0 ? 1 : 0)));
        query.pageVo.setHasMore(query.pageVo.getCurrentPage() < query.pageVo.getTotalPage());
    }

    /**
     * 获取子对象属性值
     */
    public static JSONObject getObject(Entity entity, String tableAliasName, ResultSet resultSet) throws SQLException {
        JSONObject subObject = new JSONObject();
        for (Property property : entity.properties) {
            String columnName = tableAliasName + "_" + property.column;
            switch (property.simpleTypeName) {
                case "boolean": {
                    subObject.put(property.name, resultSet.getBoolean(columnName));
                }
                break;
                case "int":
                case "integer": {
                    subObject.put(property.name, resultSet.getInt(columnName));
                }
                break;
                case "float": {
                    subObject.put(property.name, resultSet.getFloat(columnName));
                }
                break;
                case "long": {
                    subObject.put(property.name, resultSet.getLong(columnName));
                }
                break;
                case "double": {
                    subObject.put(property.name, resultSet.getDouble(columnName));
                }
                break;
                case "string": {
                    subObject.put(property.name, resultSet.getString(columnName));
                }
                break;
                default: {
                    subObject.put(property.name, resultSet.getObject(columnName));
                }
            }
        }
        return subObject;
    }

    /**将结果集映射到JSONArray中*/
    private JSONArray mappingResultSetToJSONArray(ResultSet resultSet) throws SQLException {
        int count = (int) count();
        JSONArray array = new JSONArray(count);
        ResultSetMetaData metaData = resultSet.getMetaData();
        String[] columnNames = new String[metaData.getColumnCount()];
        for (int i = 1; i <= columnNames.length; i++) {
            String label = metaData.getColumnLabel(i).toLowerCase();
            if(label.startsWith(Condition.mainTableAlias+"_")){
                label = StringUtil.Underline2Camel(label.substring(Condition.mainTableAlias.length()+1));
            }
            columnNames[i - 1] = label;
        }
        while (resultSet.next()) {
            JSONObject o = new JSONObject();
            for (int i = 1; i <= columnNames.length; i++) {
                o.put(columnNames[i - 1], resultSet.getString(i));
            }
            array.add(o);
        }
        return array;
    }

    /**将结果集映射到JSONArray中*/
    private void mappingCompositResultSetToJSONArray(ResultSet resultSet, JSONObject o) throws SQLException {
        for (SubQuery subQuery : query.subQueryList) {
            if(null==subQuery.compositField||subQuery.compositField.isEmpty()) {
                continue;
            }
            JSONObject subObject = getObject(subQuery.entity, subQuery.tableAliasName, resultSet);
            SubQuery parentSubQuery = subQuery.parentSubQuery;
            if (parentSubQuery == null) {
                o.put(subQuery.compositField, subObject);
            } else {
                List<String> fieldNames = new ArrayList<>();
                while (parentSubQuery != null) {
                    fieldNames.add(parentSubQuery.compositField);
                    parentSubQuery = parentSubQuery.parentSubQuery;
                }
                JSONObject oo = o;
                for (int i = fieldNames.size() - 1; i >= 0; i--) {
                    oo = oo.getJSONObject(fieldNames.get(i));
                }
                oo.put(subQuery.compositField, subObject);
            }
        }
    }
}
