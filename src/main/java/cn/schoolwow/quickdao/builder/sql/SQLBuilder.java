package cn.schoolwow.quickdao.builder.sql;

import java.sql.PreparedStatement;

public interface SQLBuilder {
    /**根据唯一性约束查询*/
    PreparedStatement selectByUniqueKey(Object instance) throws Exception;
}
