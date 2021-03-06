package com.paladin.framework.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommonOrderBy {

    /*
     * 排序字段和排序类型需要一一对应例如  [createTime,userName] - [1,1]
     * 排序类型见
     */

    String[] property();

    OrderType[] type();

    /**
     * 如果为true，其他动态的排序都会在该排序之后
     *
     * @return
     */
    boolean orderByFirst() default false;

}
