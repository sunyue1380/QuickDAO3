package cn.schoolwow.quickdao.handler;

import cn.schoolwow.quickdao.annotation.IdStrategy;

/**自定义列属性*/
public interface TablePropertyDefiner {
    /**是否id属性*/
    TablePropertyDefiner id(boolean id);
    /**指定id生成策略*/
    TablePropertyDefiner strategy(IdStrategy idStrategy);
    /**类型*/
    TablePropertyDefiner columnType(String columnType);
    /**列名*/
    TablePropertyDefiner columnName(String columnName);
    /**注释*/
    TablePropertyDefiner comment(String comment);
    /**是否非空*/
    TablePropertyDefiner notNull(boolean notNull);
    /**是否唯一*/
    TablePropertyDefiner unique(boolean unique);
    /**是否建立索引*/
    TablePropertyDefiner index(boolean index);
    /**是否主键*/
    TablePropertyDefiner primaryKey(boolean primaryKey);
    /**check约束*/
    TablePropertyDefiner check(String check);
    /**默认值*/
    TablePropertyDefiner defaultValue(String defaultValue);
    /**结束*/
    TableDefiner done();
}
