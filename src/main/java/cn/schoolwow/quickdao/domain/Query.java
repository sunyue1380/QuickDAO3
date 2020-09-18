package cn.schoolwow.quickdao.domain;

import cn.schoolwow.quickdao.builder.sql.dql.DQLSQLBuilder;
import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.dao.condition.AbstractCondition;
import cn.schoolwow.quickdao.dao.response.UnionType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询
 */
public class Query implements Serializable {
    /**
     * 关联Entity
     */
    public Entity entity;
    /**
     * 主表别名
     */
    public String tableAliasName = "t";
    /**
     * distinct
     */
    public String distinct = "";
    /**
     * 列名
     */
    public StringBuilder columnBuilder = new StringBuilder();
    /**
     * 字段插入
     */
    public StringBuilder insertBuilder = new StringBuilder();
    /**
     * 字段更新
     */
    public StringBuilder setBuilder = new StringBuilder();
    /**
     * 查询条件
     */
    public StringBuilder whereBuilder = new StringBuilder();
    /**
     * 分组查询
     */
    public StringBuilder groupByBuilder = new StringBuilder();
    /**
     * having查询
     */
    public StringBuilder havingBuilder = new StringBuilder();
    /**
     * 排序
     */
    public StringBuilder orderByBuilder = new StringBuilder();
    /**
     * 分页
     */
    public String limit = "";
    /**
     * 是否返回复杂属性
     */
    public boolean compositField;
    /**
     * union类型
     * */
    public UnionType unionType;
    /**
     * union语句列表
     * */
    public List<AbstractCondition> unionList = new ArrayList<>();
    /**
     * or查询语句列表
     * */
    public List<AbstractCondition> orList = new ArrayList<>();
    /**
     * 参数索引
     */
    public int parameterIndex = 1;
    /**
     * 查询参数
     */
    public List parameterList = new ArrayList();
    /**
     * 插入参数
     */
    public List insertParameterList = new ArrayList();
    /**
     * 更新参数
     */
    public List updateParameterList = new ArrayList();
    /**
     * having参数
     */
    public List havingParameterList = new ArrayList();
    /**
     * 分页对象
     * */
    public PageVo pageVo;
    /**
     * 关联子查询
     */
    public List<SubQuery> subQueryList = new ArrayList<>();
    /**
     * 关联QuickDAOConfig
     * */
    public QuickDAOConfig quickDAOConfig;
    /**
     * 关联DAO
     * */
    public DAO dao;
    /**
     * DQL查询语句构建
     * */
    public DQLSQLBuilder dqlsqlBuilder;
}
