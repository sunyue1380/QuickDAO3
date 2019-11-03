package cn.schoolwow.quickdao.handler;

import cn.schoolwow.quickdao.domain.Property;

/**数据库表的列定义*/
public class DefaultTablePropertyDefiner implements TablePropertyDefiner{
    /**当前属性*/
    private Property property;
    /**用于返回表*/
    private TableDefiner tableDefiner;

    public DefaultTablePropertyDefiner(Property property, TableDefiner tableDefiner) {
        this.property = property;
        this.tableDefiner = tableDefiner;
    }

    @Override
    public TablePropertyDefiner id(boolean id) {
        property.id = true;
        return this;
    }

    @Override
    public TablePropertyDefiner columnType(String columnType) {
        property.columnType = columnType;
        return this;
    }

    @Override
    public TablePropertyDefiner columnName(String columnName) {
        property.column = columnName;
        return this;
    }

    @Override
    public TablePropertyDefiner comment(String comment) {
        property.comment = comment;
        return this;
    }

    @Override
    public TablePropertyDefiner notNull(boolean notNull) {
        property.notNull = notNull;
        return this;
    }

    @Override
    public TablePropertyDefiner unique(boolean unique) {
        property.unique = unique;
        return this;
    }

    @Override
    public TablePropertyDefiner primaryKey(boolean primaryKey) {
        property.id = true;
        return this;
    }

    @Override
    public TablePropertyDefiner check(String check) {
        property.check = check;
        return this;
    }

    @Override
    public TablePropertyDefiner defaultValue(String defaultValue) {
        property.defaultValue = defaultValue;
        return this;
    }

    @Override
    public TableDefiner done() {
        return tableDefiner;
    }
}
