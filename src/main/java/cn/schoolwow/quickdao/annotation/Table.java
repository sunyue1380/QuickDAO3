package cn.schoolwow.quickdao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**指定表相关属性*/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    /**
     * 指定表引擎
     */
    String engine() default "";
    /**
     * 指定表编码格式
     */
    String charset() default "";
}
