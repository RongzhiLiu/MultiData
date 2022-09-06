package com.lrz.multi.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author And Date: liurongzhi on 2020/4/15.
 * Description: com.yilan.sdk.common.event
 */
@Documented
@Inherited
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface Get {
    String name();

    int defaultInt() default 0;

    boolean defaultBoolean() default false;

    float defaultFloat() default 0.0f;

    double defaultDouble() default 0.0;

    long defaultLong() default 0L;

    String defaultString() default "";
}
