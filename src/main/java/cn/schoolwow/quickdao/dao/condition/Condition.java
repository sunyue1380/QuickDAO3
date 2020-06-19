package cn.schoolwow.quickdao.dao.condition;

import cn.schoolwow.quickdao.dao.condition.subCondition.SubCondition;
import cn.schoolwow.quickdao.dao.response.Response;
import cn.schoolwow.quickdao.dao.response.UnionType;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**查询接口*/
public interface Condition<T> {
    /**
     * 手动设置主表别名
     * @param tableAliasName 主表别名
     */
    Condition<T> tableAliasName(String tableAliasName);
    /**
     * 添加distinct关键字
     */
    Condition<T> distinct();

    /**
     * 添加空查询
     * @param field 指明哪个字段为Null
     */
    Condition<T> addNullQuery(String field);

    /**
     * 添加非空查询
     * @param field 指明哪个字段不为Null
     */
    Condition<T> addNotNullQuery(String field);

    /**
     * 添加空查询
     * @param field 指明哪个字段不为空字符串
     */
    Condition<T> addEmptyQuery(String field);

    /**
     * 添加非空查询
     * @param field 指明哪个字段不为空字符串
     */
    Condition<T> addNotEmptyQuery(String field);

    /**
     * 添加范围查询语句
     * @param field  字段名
     * @param values 指明在该范围内的值
     */
    Condition<T> addInQuery(String field, Object[] values);

    /**
     * 添加范围查询语句
     * @param field  字段名
     * @param values 指明在该范围内的值
     */
    Condition<T> addInQuery(String field, List values);

    /**
     * 添加范围查询语句
     * @param field  字段名
     * @param values 指明在不该范围内的值
     */
    Condition<T> addNotInQuery(String field, Object[] values);

    /**
     * 添加范围查询语句
     * @param field  字段名
     * @param values 指明在不该范围内的值
     */
    Condition<T> addNotInQuery(String field, List values);

    /**
     * 添加between语句
     * @param field 字段名
     * @param start 范围开始值
     * @param end   范围结束值
     */
    Condition<T> addBetweenQuery(String field, Object start, Object end);

    /**
     * 添加Like查询
     * @param field 字段名
     * @param value 字段值
     */
    Condition<T> addLikeQuery(String field, Object value);

    /**
     * 添加自定义查询条件
     * <p>调用此方法您需要知道以下几点
     * <ol>
     *     <li>实体类字段使用驼峰式命名映射到数据库中.例如字段<b>firstName</b>映射到数据库后字段名为<b>first_name</b></li>
     *     <li>对于所有的查询语句,主表别名为t,使用join方法添加的表按照添加顺序依次为t1,t2,t3......</li>
     *     <li>返回字段名称均为<b>表别名_字段名</b>.例如主表中的firstName字段,则对应数据库返回列名为<b>t_first_name</b></li>
     *     <li>本方法的query参数将直接拼接到sql字符上,不会做任何转义操作,请注意SQL注入等安全问题</li>
     * </ol>
     * </p>
     * @param query 子查询条件,可使用?占位符
     * @param parameterList 占位符参数列表
     */
    Condition<T> addQuery(String query, List parameterList);

    /**
     * 添加字段查询
     * @param field 字段名
     * @param value 字段值
     */
    Condition<T> addQuery(String field, Object value);

    /**
     * 添加字段查询
     * @param field    字段名
     * @param operator 操作符,可为<b>></b>,<b>>=</b>,<b>=</b>,<b><</b><b><=</b>
     * @param value    字段值
     */
    Condition<T> addQuery(String field, String operator, Object value);

    /**
     * 添加自定义查询条件<br/>
     * <code>
     * {<br/>
     * {field}:{value},字段查询<br/>
     * {field}Start:{value},添加大于等于查询<br/>
     * {field}End:{value},添加小于等于查询<br/>
     * {field}LIKE:{value},添加Like查询<br/>
     * {field}IN:[array],添加IN查询<br/>
     * {field}NOTNULL:{value},添加not null查询<br/>
     * {field}NULL:{value},添加null查询<br/>
     * _orderBy:{value},升序排列<br/>
     * _orderByDesc:{value},降序排列<br/>
     * _pageNumber:{value},页码<br/>
     * _pageSize:{value},每页个数<br/>
     * //关联查询部分
     * _joinTables:[
     * {
     * _class:{className} 关联类,例如<b>top.cqscrb.courage.entity.User</b><br/>
     * _primaryField:{primaryField} 主表关联字段<br/>
     * _joinTableField:{joinTableField} 子表关联字段<br/>
     * {field}:{value},字段查询<br/>
     * {field}Start:{value},添加大于等于查询<br/>
     * {field}End:{value},添加小于等于查询<br/>
     * {field}LIKE:{value},添加like查询<br/>
     * {field}IN:[array],添加IN查询<br/>
     * {field}NOTNULL:{value},添加not null查询<br/>
     * {field}NULL:{value},添加null查询<br/>
     * _orderBy:{value},升序排列<br/>
     * _orderByDesc:{value},降序排列<br/>
     * _joinTables:[...]
     * }
     * ]
     * }<br/>
     * </code>
     */
    Condition<T> addJSONObjectQuery(JSONObject queryCondition);

    /**
     * 添加联合查询,需确保返回字段个数和含义一致
     * 默认union类型为 union
     * @param condition 联合查询条件
     */
    Condition<T> union(Condition<T> condition);

    /**
     * 添加联合查询,需确保返回字段个数和含义一致
     * @param condition 联合查询条件
     * @param unionType 指定union查询类型
     * @see UnionType
     */
    Condition<T> union(Condition<T> condition, UnionType unionType);

    /**
     * 添加Or查询条件
     * <p>在返回Condition对象上添加的查询条件,会以or(......)的方式拼接到SQL字符串上.</p>
     * <code>
     *     Condition or = dao.query(User.class).or();<br/>
     *     or.addQuery("username","quickdao").addQuery("password","123456");
     * </code>
     * <p>以上代码最终拼接字符串为</p>
     * <code>or (t.username = 'quickado' and t.password = '123456')</code>
     */
    Condition<T> or();

    /**
     * 添加更新字段,用于{@link cn.schoolwow.quickdao.dao.response.Response#update()}方法
     *
     * @param field 待更新的字段
     * @param value 待更新字段的值
     */
    Condition<T> addUpdate(String field, Object value);

    /**
     * <p>添加聚合字段,用于{@link cn.schoolwow.quickdao.dao.response.Response#getAggerateList()}</p>
     * <p>默认返回字段名为<b>aggerate(field)</b></p>
     * @param aggregate 聚合函数,例如COUNT,SUM,MAX,MIN,AVG
     * @param field    字段名
     */
    Condition<T> addAggregate(String aggregate, String field);

    /**
     * <p>添加聚合字段,用于{@link cn.schoolwow.quickdao.dao.response.Response#getAggerateList()}</p>
     *
     * @param aggregate COUNT,SUM,MAX,MIN,AVG
     * @param field    字段名
     * @param alias    聚合字段别名
     */
    Condition<T> addAggregate(String aggregate, String field, String alias);

    /**
     * 添加分组查询
     * @param field 分组字段
     */
    Condition<T> groupBy(String field);

    /**
     * 添加分组查询
     * @param fields 分组字段
     */
    Condition<T> groupBy(String[] fields);

    /**
     * 添加having查询,最终拼接为 {{aggregate}}({{field}}) = {{value}}
     * @param aggregate 聚合函数
     * @param field 字段名
     * @param value 字段值
     */
    Condition<T> having(String aggregate, String field, Object value);

    /**
     * 添加having查询,最终拼接为 {{field}} {{operator}} {{aggregate}}({{targetField}})
     * @param sourceAggregate 聚合函数
     * @param sourceField 字段名
     * @param operator 操作符,可为<b>></b>,<b>>=</b>,<b>=</b>,<b><</b><b><=</b>
     * @param value 字段值
     */
    Condition<T> having(String sourceAggregate, String sourceField, String operator, Object value);

    /**
     * 添加having查询,最终拼接为 {{sourceAggregate}}({{sourceField}}) = {{targetAggregate}}({{targetField}})
     * @param sourceAggregate 源字段聚合函数,可为null或者空字符串
     * @param sourceField 源字段
     * @param targetAggregate 目标字段聚合函数,可为null或者空字符串
     * @param targetField 目标字段
     */
    Condition<T> having(String sourceAggregate, String sourceField, String targetAggregate, String targetField);

    /**
     * 添加having查询,最终拼接为 {{sourceAggregate}}({{sourceField}}) {{operator}} {{targetAggregate}}({{targetField}})
     * @param sourceAggregate 源字段聚合函数,可为null或者空字符串
     * @param sourceField 源字段
     * @param operator 操作符,可为<b>></b>,<b>>=</b>,<b>=</b>,<b><</b><b><=</b>
     * @param targetAggregate 目标字段聚合函数,可为null或者空字符串
     * @param targetField 目标字段
     */
    Condition<T> having(String sourceAggregate, String sourceField, String operator, String targetAggregate, String targetField);

    /**
     * 添加having查询
     * <p>调用此方法您需要知道以下几点
     * <ol>
     *     <li>实体类字段使用驼峰式命名映射到数据库中.例如字段<b>firstName</b>映射到数据库后字段名为<b>first_name</b></li>
     *     <li>对于所有的查询语句,主表别名为t,使用join方法添加的表按照添加顺序依次为t1,t2,t3......</li>
     *     <li>返回字段名称均为<b>表别名_字段名</b>.例如主表中的firstName字段,则对应数据库返回列名为<b>t_first_name</b></li>
     *     <li>本方法的having参数将直接拼接到sql字符上,不会做任何转义操作,请注意SQL注入等安全问题</li>
     * </ol>
     * </p>
     * @param having having查询子句,可使用?占位符
     * @param parameterList  占位符参数值
     */
    Condition<T> addHaving(String having, List parameterList);

    /**
     * 关联表查询
     * <ul>
     *     <li>主表,表别名为t</li>
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
     */
    SubCondition<T> joinTable(Class clazz, String primaryField, String joinTableField);

    /**
     * 关联表查询
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
     */
    SubCondition<T> joinTable(Class clazz, String primaryField, String joinTableField, String compositField);

    /**
     * 根据指定字段升序排列
     *
     * @param field 升序排列字段名
     */
    Condition<T> orderBy(String field);

    /**
     * 根据指定字段降序排列
     *
     * @param field 降序排列字段名
     */
    Condition<T> orderByDesc(String field);

    /**
     * 分页操作
     *
     * @param offset 偏移量
     * @param limit  返回个数
     */
    Condition<T> limit(long offset, long limit);

    /**
     * 分页操作
     *
     * @param pageNum  第几页
     * @param pageSize 每页个数
     */
    Condition<T> page(int pageNum, int pageSize);

    /**
     * 部分查询,用于<b>{@link cn.schoolwow.quickdao.dao.response.Response#getPartList()}</b>
     *
     * @param field 待返回字段名数组
     */
    Condition<T> addColumn(String field);

    /**
     * 部分查询,用于<b>{@link cn.schoolwow.quickdao.dao.response.Response#getPartList()}</b>
     *
     * @param fields 待返回字段名数组
     */
    Condition<T> addColumns(String[] fields);

    /**
     * 排除部分字段,用于<b>{@link cn.schoolwow.quickdao.dao.response.Response#getPartList()}</b>
     *
     * @param field 待返回字段名数组
     */
    Condition<T> excludeColumn(String field);

    /**
     * 排除部分字段,用于<b>{@link cn.schoolwow.quickdao.dao.response.Response#getPartList()}</b>
     *
     * @param fields 待返回字段名数组
     */
    Condition<T> excludeColumn(String[] fields);

    /**
     * 自定义查询列,用于<b>{@link cn.schoolwow.quickdao.dao.response.Response#getSpecialList()}</b>
     *
     * @param field 自定义查询列
     */
    Condition<T> addSpecialColumn(String field);

    /**
     * 自定义查询列,用于<b>{@link cn.schoolwow.quickdao.dao.response.Response#getSpecialList()} ()}</b>
     *
     * @param fields 自定义查询列
     */
    Condition<T> addSpecialColumns(String[] fields);

    /**
     * 返回子表实体类字段信息
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#joinTable(Class, String, String)} ()}
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#joinTable(Class, String, String,String)} ()}
     */
    Condition<T> compositField();

    /**
     * 调用or()方法后返回主Condition
     */
    Condition<T> done();

    /**
     * 执行并返回Response实例
     */
    Response<T> execute();
}
