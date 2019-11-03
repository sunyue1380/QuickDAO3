package cn.schoolwow.quickdao.handler;

import cn.schoolwow.quickdao.QuickDAO;

/**自定义表*/
public interface TableDefiner {
    /**映射表名*/
    TableDefiner tableName(String tableName);
    /**注释*/
    TableDefiner comment(String comment);
    /**处理列*/
    TablePropertyDefiner property(String fieldName);
    /**结束*/
    QuickDAO done();
}
