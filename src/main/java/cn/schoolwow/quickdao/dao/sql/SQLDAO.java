package cn.schoolwow.quickdao.dao.sql;

public interface SQLDAO {
    /**
     * 实例对象是否存在
     * @param instance 实例对象
     */
    boolean exist(Object instance);
}
