package cn.schoolwow.quickdao.dao.condition;

import cn.schoolwow.quickdao.dao.condition.subCondition.SubCondition;
import cn.schoolwow.quickdao.dao.response.Response;
import cn.schoolwow.quickdao.dao.response.UnionType;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.util.Collection;

/**
 * 查询接口
 *
 * <p>本接口定义里单表查询接口,多表关联查询等方法</p>
 *
 * <p>对于查询接口您需要知道以下几点</p>
 * <ol>
 *     <li>实体类字段使用驼峰式命名映射到数据库中.例如字段<b>firstName</b>映射到数据库后字段名为<b>first_name</b>.
 *     <br/><b>如您使用了@ColumnName注解则以该注解设置的字段为准</b>
 *     </li>
 *     <li>对于关联查询,主表别名为t,使用join方法添加的子表按照添加顺序依次为t1,t2,t3......
 *     <br/><b>如您使用了tableAliasName方法则以该方法设置的值为准</b>
 *     <p></p>
 *     <p>关联查询默认关联主表中唯一一个类型为子表的成员变量.</p>
 *     <pre>
 *         Parent{
 *             long id;
 *         }
 *         Child{
 *             long id;
 *             long parentId;
 *             Parent parent;
 *         }
 *         //关联Child类的唯一一个子表成员变量的parent属性
 *         joinTable(Parent.class,"parentId","id").compositField();
 *     </pre>
 *     <p>若出现多个相同的子表成员变量,必须手动指定相应成员变量</p>
 *     <pre>
 *         Parent{
 *             long id;
 *         }
 *         Child{
 *             long id;
 *             long fatherId;
 *             long motherId;
 *
 *             Parent father;
 *             Parent mother;
 *         }
 *         //关联多个时需要手动指定
 *         joinTable(Parent.class,"fatherId","id","father").compositField();
 *         joinTable(Parent.class,"motherId","id","mother").compositField();
 *     </pre>
 *     <p>若需返回关联实体字段,必须调用<b>compositField</b>方法</p>
 *     </li>
 *     <li>当您调用getArray方法,返回字段名称均为<b>表别名_字段名</b>.例如主表中的firstName字段,则返回字段名称为<b>t_first_name</b></li>
 *     <li>对于直接添加查询语句的方法,请注意查询语句参数将直接拼接到sql字符上,不会做任何转义操作,请注意SQL注入等安全问题</li>
 * </ol>
 * </p>
 */
public interface Condition<T> extends Serializable {
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
    Condition<T> addInQuery(String field, Object... values);

    /**
     * 添加范围查询语句
     * @param field  字段名
     * @param values 指明在该范围内的值
     */
    Condition<T> addInQuery(String field, Collection values);

    /**
     * 添加范围查询语句
     * @param field  字段名
     * @param values 指明在不该范围内的值
     */
    Condition<T> addNotInQuery(String field, Object... values);

    /**
     * 添加范围查询语句
     * @param field  字段名
     * @param values 指明在不该范围内的值
     */
    Condition<T> addNotInQuery(String field, Collection values);

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
     * 自定义查询语句,具体映射规则请看此{@link cn.schoolwow.quickdao.dao.condition.Condition}
     * @param query 子查询条件,可使用?占位符
     * @param parameterList 占位符参数列表,可为null
     */
    Condition<T> addRawQuery(String query, Object... parameterList);

    /**
     * 添加where子查询
     * @param field    字段名
     * @param operator 操作符,可为<b>></b>,<b>>=</b>,<b>=</b>,<b><</b><b><=</b>
     * @param subQuery 子查询语句
     */
    Condition<T> addSubQuery(String field, String operator, Condition subQuery);

    /**
     * 添加exist子查询
     * @param subQuery 子查询语句
     */
    Condition<T> addExistSubQuery(Condition subQuery);

    /**
     * 添加not exist子查询
     * @param subQuery 子查询语句
     */
    Condition<T> addNotExistSubQuery(Condition subQuery);

    /**
     * 添加自定义字段,具体映射规则请看此{@link cn.schoolwow.quickdao.dao.condition.Condition}
     * @param fields 自定义查询列
     */
    Condition<T> addColumn(String... fields);

    /**
     * 添加select子查询
     * @param subQuery 子查询
     */
    Condition<T> addColumn(Condition subQuery);

    /**
     * 添加插入字段,用于{@link cn.schoolwow.quickdao.dao.response.Response#insert()}方法
     *
     * @param field 待更新的字段
     * @param value 待更新字段的值
     */
    Condition<T> addInsert(String field, Object value);

    /**
     * 添加更新字段,用于{@link cn.schoolwow.quickdao.dao.response.Response#update()}方法
     *
     * @param field 待更新的字段
     * @param value 待更新字段的值
     */
    Condition<T> addUpdate(String field, Object value);

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
     * 添加Or查询条件,返回一个新的OrCondition对象
     * <p>在返回Condition对象上添加的查询条件,会以or(......)的方式拼接到SQL字符串上.</p>
     * <pre>
     *     Condition condition= dao.query(User.class);
     *     condition.or().addQuery("username","quickdao")
     *               .addQuery("password","123456");
     *     //继续对主condition进行操作
     *     condition.....
     * </pre>
     * <p>以上代码最终拼接字符串为</p>
     * <code>or (t.username = 'quickado' and t.password = '123456')</code>
     */
    Condition<T> or();

    /**
     * 添加or查询,具体映射规则请看此{@link cn.schoolwow.quickdao.dao.condition.Condition}
     * @param or or查询语句,可使用?占位符
     * @param parameterList  占位符参数值,无参数时可为null
     */
    Condition<T> or(String or, Object... parameterList);

    /**
     * 添加分组查询
     * @param fields 分组字段
     */
    Condition<T> groupBy(String... fields);

    /**
     * 添加having查询
     * <p>调用本方法时请先查看Condition类JavaDoc注释</p>
     *
     * @param having having查询子句,可使用?占位符
     * @param parameterList  占位符参数值
     * @see Condition
     */
    Condition<T> having(String having, Object... parameterList);

    /**
     * 添加having子查询
     * <p>调用本方法时请先查看Condition类JavaDoc注释</p>
     * @param field    字段名
     * @param operator 操作符,可为<b>></b>,<b>>=</b>,<b>=</b>,<b><</b><b><=</b>
     * @param subQuery 子查询语句
     */
    Condition<T> having(String field, String operator, Condition subQuery);

    /**
     * cross join 关联表,获取笛卡尔积
     * <p>调用本方法时请先查看Condition类JavaDoc注释</p>
     * @param clazz 待关联的子表
     */
    <E> SubCondition<E> crossJoinTable(Class<E> clazz);

    /**
     * cross join 关联表,获取笛卡尔积
     * <p>调用本方法时请先查看Condition类JavaDoc注释</p>
     * @param tableName 待关联的子表
     */
    <E> SubCondition<E> crossJoinTable(String tableName);

    /**
     * 关联表查询
     * <p>调用本方法时请先查看Condition类JavaDoc注释</p>
     * @param clazz 待关联的子表
     * @param primaryField   <b>主表</b>关联字段
     * @param joinTableField <b>子表</b>关联字段
     */
    <E> SubCondition<E> joinTable(Class<E> clazz, String primaryField, String joinTableField);

    /**
     * 关联表查询
     * <p>调用本方法时请先查看Condition类JavaDoc注释</p>
     * @param clazz 待关联的子表
     * @param primaryField   <b>主表</b>关联字段
     * @param joinTableField <b>子表</b>关联字段
     * @param compositField <b>子表</b>实体类成员变量名
     */
    <E> SubCondition<E> joinTable(Class<E> clazz, String primaryField, String joinTableField, String compositField);

    /**
     * 关联子查询
     * <p>调用本方法时请先查看Condition类JavaDoc注释</p>
     * <p>调用本方法将在sql语句中拼接如下字符串</p>
     * <p><b>join #{condition子表} as t1 on t.primaryField = t1.joinTableField</b>
     * </p>
     *
     * @param joinCondition 关联Condition
     * @param primaryField   <b>主表</b>关联字段
     * @param joinConditionField <b>子查询</b>关联字段
     */
    <E> SubCondition<E> joinTable(Condition<E> joinCondition, String primaryField, String joinConditionField);

    /**
     * 关联表查询
     * <p>调用本方法时请先查看Condition类JavaDoc注释</p>
     * <p>调用本方法将在sql语句中拼接如下字符串</p>
     * <p><b>join #{clazz} as t1 on t.primaryField = t1.joinTableField</b>
     * </p>
     * @param tableName 待关联的子表
     * @param primaryField   <b>主表</b>关联字段
     * @param joinTableField <b>子表</b>关联字段
     */
    SubCondition joinTable(String tableName, String primaryField, String joinTableField);

    /**
     * 根据指定字段升序排列
     * <b>注意</b>若调用了addColumn方法,请将调用本方法放在addColumn方法之后
     *
     * @param field 升序排列字段名
     */
    Condition<T> orderBy(String... field);

    /**
     * 根据指定字段降序排列
     * <b>注意</b>若调用了addColumn方法,请将调用本方法放在addColumn方法之后
     * @param field 降序排列字段名
     */
    Condition<T> orderByDesc(String... field);

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
     * 返回子表实体类字段信息
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#joinTable(Class, String, String)} ()}
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#joinTable(Class, String, String,String)} ()}
     */
    Condition<T> compositField();

    /**
     * 执行并返回Response实例
     */
    Response<T> execute();

    /**
     * 执行并返回Response实例
     */
    Condition<T> clone();
}
