package cn.schoolwow.quickdao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段约束
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Constraint {
    /**是否非空*/
    boolean notNull() default false;
    /**是否唯一*/
    boolean unique() default false;
    /**Check约束*/
    String check() default "";
    /**默认值*/
    String defaultValue() default "";
    /**是否建立联合唯一约束*/
    boolean unionUnique() default true;
}
