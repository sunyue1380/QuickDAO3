package cn.schoolwow.quickdao.dao.response;

import cn.schoolwow.quickdao.domain.Query;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Set;

public class H2Response extends AbstractResponse {

    public H2Response(Query query) {
        super(query);
    }

    @Override
    public JSONArray getAggerateList() {
        JSONArray array = new JSONArray((int) count());
        try {
            PreparedStatement ps = query.dqlsqlBuilder.getAggerateList(query);
            ResultSet resultSet = ps.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                JSONObject o = new JSONObject();
                for (int i = 1; i <= columnCount; i++) {
                    o.put(metaData.getColumnName(i).toLowerCase(), resultSet.getString(i));
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
    public JSONArray getSpecialList() {
        JSONArray array = super.getSpecialList();
        for(int i=0;i<array.size();i++){
            JSONObject o = array.getJSONObject(i);
            Set<String> set = o.keySet();
            for(String key:set){
                o.put(key.toLowerCase(),o.get(key));
            }
        }
        return array;
    }
}
