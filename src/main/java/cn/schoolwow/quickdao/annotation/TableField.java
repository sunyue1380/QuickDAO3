package cn.schoolwow.quickdao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**数据库列属性*/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableField {
    /**字段函数*/
    String function() default "";
    /**是否填充插入时间*/
    boolean createdAt() default false;
    /**是否填充更新时间*/
    boolean updatedAt() default false;
}
