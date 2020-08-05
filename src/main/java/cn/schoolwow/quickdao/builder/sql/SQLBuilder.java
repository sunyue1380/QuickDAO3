package cn.schoolwow.quickdao.builder.sql;

import java.sql.PreparedStatement;

public interface SQLBuilder {
    /**根据唯一性约束查询*/
    PreparedStatement selectCountById(Object instance) throws Exception;
    /**根据唯一性约束查询*/
    PreparedStatement selectCountByUniqueKey(Object instance) throws Exception;
}
