package com.lrz.multi.annotation;



import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author And Date: liurongzhi on 2020/4/15.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Table {
    String name() default "DEFAULT_TABLE";

    /**
     * 是否延迟从磁盘中初始化，等到需要使用时再加载
     * 磁盘懒加载
     *
     * @return 是否懒加载磁盘
     */
    boolean lazy() default false;
}
