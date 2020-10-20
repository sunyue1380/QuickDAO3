# id生成策略

QuickDAO目前支持三种id生成策略

```java
/**ID生成策略*/
public enum IdStrategy {
    /**用户自己处理*/
    None,
    /**自增(默认策略)*/
    AutoIncrement,
    /**使用ID生成器*/
    IdGenerator;
}
```

您可以通过在字段上添加@Id注解手动指定id生成策略,也可以在配置DAO时指定全局IdStrategy策略.

@Id注解配置优先于全局Id生成策略

# id生成器

目前QuickDAO只支持一种Id生成器,SnowflakeIdGenerator(雪花算法Id生成器)