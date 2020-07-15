package cn.schoolwow.quickdao.annotation;

/**ID生成策略*/
public enum IdStrategy {
    /**用户自己处理*/
    None,
    /**自增(默认策略)*/
    AutoIncrement,
    /**使用ID生成器*/
    IdGenerator;
}
