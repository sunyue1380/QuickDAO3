package cn.schoolwow.quickdao.dao.response;

import cn.schoolwow.quickdao.domain.*;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AbstractResponse<T> implements Response<T>{
    private Logger logger = LoggerFactory.getLogger(AbstractResponse.class);
    //查询对象参数
    public Query query;

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
    public int insert() {
        int count = 0;
        try {
            if(null!=query.insertArray){
                PreparedStatement[] preparedStatements = query.dqlsqlBuilder.insertArray(query);
                for(int i=0;i<preparedStatements.length;i++){
                    count += preparedStatements[i].executeUpdate();
                    if(count>0){
                        ResultSet rs = preparedStatements[i].getGeneratedKeys();
                        if (rs.next()) {
                            query.insertArray.getJSONObject(i).put("generatedKeys",rs.getString(1));
                        }
                        rs.close();
                    }
                    preparedStatements[i].close();
                }
                query.dqlsqlBuilder.connection.commit();
            }else{
                PreparedStatement ps = query.dqlsqlBuilder.insert(query);
                count = ps.executeUpdate();
                if (count>0&&null!=query.insertValue) {
                    ResultSet rs = ps.getGeneratedKeys();
                    if (rs.next()) {
                        query.insertValue.put("generatedKeys",rs.getString(1));
                    }
                    rs.close();
                }
                ps.close();
            }
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",count+"");
        return count;
    }

    @Override
    public int update() {
        int count = 0;
        try {
            PreparedStatement ps = query.dqlsqlBuilder.update(query);
            count= ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",count+"");
        return count;
    }

    @Override
    public int delete() {
        int count = 0;
        try {
            PreparedStatement ps = query.dqlsqlBuilder.delete(query);
            count= ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",count+"");
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
    public <E> E getOne(Class<E> clazz) {
        List<E> list = getList(clazz);
        if (list == null || list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public <E> E getSingleColumn(Class<E> clazz) {
        List<E> list = getSingleColumnList(clazz);
        if (list == null || list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public <E> List<E> getSingleColumnList(Class<E> clazz) {
        try {
            PreparedStatement ps = query.dqlsqlBuilder.getArray(query);
            JSONArray array = new JSONArray(query.dqlsqlBuilder.getResultSetRowCount(query));
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
    public List getList() {
        return getList(query.entity.clazz);
    }

    @Override
    public <E> List<E> getList(Class<E> clazz) {
        return getArray().toJavaList(clazz);
    }

    @Override
    public PageVo<T> getPagingList() {
        query.pageVo.setList(getArray().toJavaList(query.entity.clazz));
        setPageVo();
        return query.pageVo;
    }

    @Override
    public JSONObject getObject() {
        JSONArray array = getArray();
        if(null==array||array.isEmpty()){
            return null;
        }
        return array.getJSONObject(0);
    }

    @Override
    public JSONArray getArray() {
        JSONArray array = null;
        try {
            PreparedStatement ps = query.dqlsqlBuilder.getArray(query);
            array = new JSONArray(query.dqlsqlBuilder.getResultSetRowCount(query));
            ResultSet resultSet = ps.executeQuery();
            if(query.columnBuilder.length()>0){
                ResultSetMetaData metaData = resultSet.getMetaData();
                String[] columnNames = new String[metaData.getColumnCount()];
                for (int i = 1; i <= columnNames.length; i++) {
                    columnNames[i - 1] = metaData.getColumnLabel(i);
                }
                while (resultSet.next()) {
                    JSONObject o = new JSONObject(true);
                    for (int i = 1; i <= columnNames.length; i++) {
                        o.put(columnNames[i - 1], resultSet.getString(i));
                    }
                    array.add(o);
                }
            }else{
                while (resultSet.next()) {
                    JSONObject o = getObject(query.entity, query.tableAliasName, resultSet);
                    if(query.compositField){
                        getCompositObject(resultSet,o);
                    }
                    array.add(o);
                }
            }
            MDC.put("count",array.size()+"");
            resultSet.close();
            ps.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return array;
    }

    /**设置分页对象*/
    private void setPageVo() {
        if (query.pageVo == null) {
            throw new IllegalArgumentException("请先调用page()函数!");
        }
        query.pageVo.setTotalSize(count());
        query.pageVo.setTotalPage((int)(query.pageVo.getTotalSize() / query.pageVo.getPageSize() + (query.pageVo.getTotalSize() % query.pageVo.getPageSize() > 0 ? 1 : 0)));
        query.pageVo.setHasMore(query.pageVo.getCurrentPage() < query.pageVo.getTotalPage());
    }

    /**
     * 获取子对象属性值
     */
    public static JSONObject getObject(Entity entity, String tableAliasName, ResultSet resultSet) throws SQLException {
        JSONObject subObject = new JSONObject(true);
        for (Property property : entity.properties) {
            String columnName = tableAliasName + "_" + property.column;
            String key = property.name==null?property.column:property.name;
            if(null==property.simpleTypeName){
                subObject.put(key, resultSet.getString(columnName));
                continue;
            }
            switch (property.simpleTypeName) {
                case "boolean": {
                    subObject.put(key, resultSet.getBoolean(columnName));
                }
                break;
                case "int":
                case "integer": {
                    subObject.put(key, resultSet.getInt(columnName));
                }
                break;
                case "float": {
                    subObject.put(key, resultSet.getFloat(columnName));
                }
                break;
                case "long": {
                    subObject.put(key, resultSet.getLong(columnName));
                }
                break;
                case "double": {
                    subObject.put(key, resultSet.getDouble(columnName));
                }
                break;
                case "string": {
                    subObject.put(key, resultSet.getString(columnName));
                }
                break;
                case "localdate": {
                    Date date = resultSet.getTimestamp(columnName);
                    LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                    subObject.put(key, localDate);
                }
                break;
                case "localdatetime": {
                    Date date = resultSet.getTimestamp(columnName);
                    LocalDateTime localDateTime = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                    subObject.put(key, localDateTime);
                }
                break;
                default: {
                    subObject.put(key, resultSet.getObject(columnName));
                }
            }
        }
        return subObject;
    }

    private void getCompositObject(ResultSet resultSet, JSONObject o) throws SQLException {
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
