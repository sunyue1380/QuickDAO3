package cn.schoolwow.quickdao.domain;

import cn.schoolwow.quickdao.annotation.ForeignKey;

/**
 * 实体类属性信息
 */
public class Property {
    /**
     * 是否是id
     */
    public boolean id;
    /**
     * 列名
     */
    public String column;
    /**
     * 自定义类型
     */
    public String columnType;
    /**
     * 类型名
     */
    public String simpleTypeName;
    /**
     * 属性名
     */
    public String name;
    /**
     * 是否建立索引
     */
    public boolean index;
    /**
     * 是否唯一
     */
    public boolean unique;
    /**
     * 是否非空
     */
    public boolean notNull;
    /**
     * check约束
     */
    public String check;
    /**
     * 默认值
     */
    public String defaultValue;
    /**
     * 注释
     */
    public String comment;
    /**
     * 外键关联
     */
    public ForeignKey foreignKey;
    /**
     * 所属实体
     * */
    public Entity entity;
}
