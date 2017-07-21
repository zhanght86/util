package com.myccnice.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * create in 2017年3月28日
 * @author wangpeng
 * @category 对象属性信息注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BeanAttrInfo {

    /** 对应Dictionary对象中value */
    String value() default "";

    /** 对应Dictionary对象中cnName */
    String cnName() default "";

    /** 对应Dictionary对象中enName */
    String enName() default "";

    /** 排序 */
    int orderBy() default 0;

    /** 是否有效：1、无效；2、有效*/
    int valid() default 2;
}
