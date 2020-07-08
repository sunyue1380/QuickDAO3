package cn.schoolwow.quickdao.dao.sql.dml;

//ID生成器接口
public interface IDGenerator {
    //获取下一个ID
    long getNextId();
}
