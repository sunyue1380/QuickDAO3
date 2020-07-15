package cn.schoolwow.quickdao.dao.condition.subCondition;

import cn.schoolwow.quickdao.dao.condition.Condition;

import java.util.List;

/**子表查询接口*/
public interface SubCondition<T>{
    /**
     * 手动设置表别名
     */
    SubCondition<T> tableAliasName(String tableAliasName);
    /**
     * 左外连接
     */
    SubCondition<T> leftJoin();
    /**
     * 右外连接
     */
    SubCondition<T> rightJoin();
    /**
     * 全外连接
     */
    SubCondition<T> fullJoin();
    /**
     * 添加空查询
     * @param field 指明哪个字段为Null
     */
    SubCondition<T> addNullQuery(String field);
    /**
     * 添加非空查询
     * @param field 指明哪个字段不为Null
     */
    SubCondition<T> addNotNullQuery(String field);
    /**
     * 添加空查询
     * @param field 指明哪个字段不为空字符串
     */
    SubCondition<T> addEmptyQuery(String field);
    /**
     * 添加非空查询
     * @param field 指明哪个字段不为空字符串
     */
    SubCondition<T> addNotEmptyQuery(String field);
    /**
     * 添加范围查询语句
     * @param field  字段名
     * @param values 指明在该范围内的值
     */
    SubCondition<T> addInQuery(String field, Object... values);
    /**
     * 添加范围查询语句
     * @param field  字段名
     * @param values 指明在该范围内的值
     */
    SubCondition<T> addInQuery(String field, List values);
    /**
     * 添加范围查询语句
     * @param field  字段名
     * @param values 指明在不该范围内的值
     */
    SubCondition<T> addNotInQuery(String field, Object... values);
    /**
     * 添加范围查询语句
     * @param field  字段名
     * @param values 指明在不该范围内的值
     */
    SubCondition<T> addNotInQuery(String field, List values);
    /**
     * 添加between语句
     * @param field 字段名
     * @param start 范围开始值
     * @param end   范围结束值
     */
    SubCondition<T> addBetweenQuery(String field, Object start, Object end);

    /**
     * 添加Like查询
     * @param field 字段名
     * @param value 字段值
     */
    SubCondition<T> addLikeQuery(String field, Object value);

    /**
     * 添加自定义查询条件
     * <p>调用此方法您需要知道以下几点
     * <ol>
     *     <li>实体类字段使用驼峰式命名映射到数据库中.例如字段<b>firstName</b>映射到数据库后字段名为<b>first_name</b></li>
     *     <li>对于所有的查询语句,主表别名为t,使用join方法添加的表按照添加顺序依次为t1,t2,t3......</li>
     *     <li>返回字段名称均为<b>表别名_字段名</b>.例如主表中的firstName字段,则对应数据库返回列名为<b>t_first_name</b></li>
     *     <li>本方法的query参数将直接拼接到sql字符上,不会做任何转义操作,请注入SQL注入安全相关问题</li>
     * </ol>
     * </p>
     * @param query 子查询条件
     */
    SubCondition<T> addQuery(String query);
    /**
     * 添加字段查询
     * @param field 字段名
     * @param value 字段值
     */
    SubCondition<T> addQuery(String field, Object value);
    /**
     * 添加字段查询
     * @param field    字段名
     * @param operator 操作符,可为<b>></b>,<b>>=</b>,<b>=</b>,<b><</b><b><=</b>
     * @param value    字段值
     */
    SubCondition<T> addQuery(String field, String operator, Object value);

    /**
     * 关联表查询,子表可再次关联子表
     * 当子表关联子表时,我们称原子表为父表,关联表为子表
     * <ul>
     *     <li>父表,调用本方法的表</li>
     *     <li>子表,依据关联顺序别名依次为t1,t2,t3......</li>
     * </ul>
     * <p>调用本方法将在sql语句中拼接如下字符串
     * <b>join #{clazz} as t1 on t.primaryField = t1.joinTableField</b>
     * </p>
     *
     * <p>本方法默认关联主表中唯一一个类型为子表的成员变量,若出现多个相同的子表成员变量,则会抛出异常.例如以下代码:</p>
     * <pre>
     *     Parent{
     *         long id;
     *     }
     *     Child{
     *         long id;
     *         long parentId;
     *         Parent parent;
     *     }
     *     joinTable(Parent.class,"parentId","id");
     * </pre>
     * <p>本方法会自动关联Child类的唯一一个子表成员变量的parent属性</p>
     * <p><b>注意:</b>需要返回关联实体类字段时,需要调用{@link Condition#compositField()}方法才会返回关联实体类对象</p>
     * @param clazz 待关联的子表
     * @param primaryField   <b>主表</b>关联字段
     * @param joinTableField <b>子表</b>关联字段
     * @see {@link Condition#joinTable(Class, String, String)}
     */
    SubCondition<T> joinTable(Class clazz, String primaryField, String joinTableField);
    /**
     * 关联表查询,手动指定子表关联字段
     * <ul>
     *     <li>主表,表别名为t</li>
     *     <li>子表,依据关联顺序别名依次为t1,t2,t3......</li>
     * </ul>
     * <p>调用本方法将在sql语句中拼接如下字符串
     * <b>join #{clazz} as t1 on t.primaryField = t1.joinTableField</b>
     * </p>
     *
     * <p>本方法用于手动指定需要关联的子表实体类成员变量名.当出现多个相同的子表成员变量,需要手动指定关联实体类成员变量.例如以下代码:</p>
     * <pre>
     *     Parent{
     *         long id;
     *     }
     *     Child{
     *         long id;
     *         long fatherId;
     *         long motherId;
     *
     *         Parent father;
     *         Parent mother;
     *     }
     *     joinTable(Parent.class,"fatherId","id","father");
     *     joinTable(Parent.class,"motherId","id","mother");
     * </pre>
     * <p><b>注意:</b>需要返回关联实体类字段时,需要调用{@link Condition#compositField()}方法才会返回关联实体类对象</p>
     * @param clazz 待关联的子表
     * @param primaryField   <b>主表</b>关联字段
     * @param joinTableField <b>子表</b>关联字段
     * @param compositField <b>子表</b>实体类成员变量名
     * @see {@link Condition#joinTable(Class, String, String, String)}
     */
    SubCondition<T> joinTable(Class clazz, String primaryField, String joinTableField, String compositField);

    /**
     * 添加分组查询
     * @param fields 分组字段
     */
    SubCondition<T> groupBy(String... fields);

    /**
     * 根据指定字段升序排列
     *
     * @param fields 升序排列字段名
     */
    SubCondition<T> orderBy(String... fields);

    /**
     * 根据指定字段降序排列
     *
     * @param fields 降序排列字段名
     */
    SubCondition<T> orderByDesc(String... fields);

    /**
     * 返回<b>父表</b>
     * @see {@link SubCondition#joinTable(Class, String, String)}
     */
    SubCondition<T> doneSubCondition();
    /**
     * 返回<b>主表</b>
     * @see {@link Condition#joinTable(Class, String, String)}
     */
    Condition<T> done();
}
