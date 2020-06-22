package cn.schoolwow.quickdao.dao.response;

import cn.schoolwow.quickdao.dao.condition.Condition;
import cn.schoolwow.quickdao.domain.PageVo;
import com.alibaba.fastjson.JSONArray;

import java.util.List;

public interface Response<T> {
    /**
     * 获取符合条件的总数目
     */
    long count();

    /**
     * <p>更新符合条件的记录</p>
     * <p><b>前置条件</b>:请先调用<b>{@link cn.schoolwow.quickdao.dao.condition.Condition#addUpdate(String, Object)}</b>方法</p>
     */
    long update();

    /**
     * 删除符合条件的数据库记录
     */
    long delete();

    /**
     * <p>获取符合条件的数据库记录的第一条</p>
     * <p>若无符合条件的数据库记录,返回Null</p>
     */
    T getOne();

    /**
     * 返回符合条件的数据库记录
     */
    List<T> getList();

    /**
     * 返回符合条件的数据库记录
     */
    JSONArray getArray();

    /**
     * <p>返回聚合字段的数据库记录</p>
     * <p>此方法会同时返回<b>addAggerate()方法</b>和<b>addColumn()方法</b>所指定的字段</p>
     * <p><b>注意:</b>调用此方法时请务必先调用{@link cn.schoolwow.quickdao.dao.condition.Condition#addAggregate(String, String)}}</p>
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#addAggregate(String, String)}
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#addColumn(String...)}}
     */
    JSONArray getAggerateList();

    /**
     * 返回聚合字段分页数据库记录.
     * <p><b>注意:</b>调用此方法时必须调用分页方法</p>
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#addAggregate(String, String)}
     */
    PageVo getAggeratePagingList();

    /**
     * 返回聚合字段分页数据库记录.
     * <p><b>注意:</b>调用此方法时必须调用分页方法</p>
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#addAggregate(String, String)}
     */
    <E> PageVo<E> getAggeratePagingList(Class<E> clazz);

    /**
     * 返回指定单个字段的集合
     *
     * @param clazz 返回字段类型
     * @param column 待返回字段名
     */
    <E> List<E> getValueList(Class<E> clazz, String column);

    /**
     * <p>返回指定字段的数据库记录</p>
     * <p>此方法会返回<b>addColumn()方法</b>所指定的字段</p>
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#addColumn(String...)})}}
     */
    List<T> getPartList();

    /**
     * <p>返回指定字段的数据库记录</p>
     * <p>此方法会返回<b>addColumn()方法</b>所指定的字段</p>
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#addColumn(String...)}}
     */
    JSONArray getSpecialList();

    /**
     * 返回符合条件的分页数据库记录.
     * <p>此方法会返回<b>addColumn()方法</b>所指定的字段</p>
     * <p><b>注意:</b>调用此方法时必须调用分页方法</p>
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#page(int, int)}
     */
    PageVo<T> getPagingList();

    /**
     * 返回符合条件的分页数据库记录.
     * <p><b>注意:</b>调用此方法时必须调用分页方法</p>
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#page(int, int)}
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#addColumn(String...)}}
     */
    PageVo<T> getPartPagingList();

    /**
     * 合并查询
     * <p><b>注意:</b>调用此方法时必须调用union方法,且多个Condition所返回的字段信息必须一致</p>
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#union(Condition)}}
     */
    List<T> getUnionList();
    /**
     * 合并分页查询
     * <p><b>注意:</b>调用此方法时必须调用分页方法</p>
     * <p><b>注意:</b>调用此方法时必须调用union方法,且多个Condition所返回的字段信息必须一致</p>
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#page(int, int)}}}
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#union(Condition)}}
     */
    PageVo<T> getUnionPagingList();
}
