package cn.schoolwow.quickdao.dao.response;

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
     * 返回查询结果的第一列
     *
     * @param clazz 返回字段类型
     */
    <E> List<E> getSingleColumnList(Class<E> clazz);

    /**
     * 返回符合条件的数据库记录
     */
    List<T> getList();

    /**
     * 返回符合条件的分页数据库记录.
     * <p>此方法会返回<b>addColumn()方法</b>所指定的字段</p>
     * <p><b>注意:</b>调用此方法时必须调用分页方法</p>
     * @see {@link cn.schoolwow.quickdao.dao.condition.Condition#page(int, int)}
     */
    PageVo<T> getPagingList();

    /**
     * 返回符合条件的数据库记录
     */
    JSONArray getArray();

}
