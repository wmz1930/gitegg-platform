package com.gitegg.platform.base.annotation.resubmit;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 防止重复提交注解
 * 1、当设置了keys时，通过表达式确定取哪几个参数作为防重key
 * 2、当未设置keys时，可以设置argsIndex设置取哪几个参数作为防重key
 * 3、argsIndex和ignoreKeys是未设置keys时生效，排除不需要防重的参数
 * 4、因部分浏览器在跨域请求时，不允许request请求携带cookie，导致每次sessionId都是新的，所以这里默认使用用户id作为key的一部分，不使用sessionId
 * @author GitEgg
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResubmitLock {

    /**
     * 防重复提交校验的时间间隔
     */
    long interval() default 5;

    /**
     * 防重复提交校验的时间间隔的单位
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 是否仅在当前session内进行防重复提交校验
     */
    boolean currentSession() default false;

    /**
     * 是否选用当前操作用户的信息作为防重复提交校验key的一部分
     */
    boolean currentUser() default true;

    /**
     * keys和ignoreKeys不能同时使用
     * 参数Spring EL表达式例如 #{param.name},表达式的值作为防重复校验key的一部分
     */
    String[] keys() default {};

    /**
     * keys和ignoreKeys不能同时使用
     * ignoreKeys不区分入参，所有入参拥有相同的字段时，都将过滤掉
     */
    String[] ignoreKeys() default {};

    /**
     * Spring EL表达式,决定是否进行重复提交校验,多个条件之间为且的关系,默认是进行校验
     */
    String[] conditions() default {"true"};

    /**
     * 当未配置key时，设置哪几个参数作为防重对象，默认取所有参数
     *
     */
    int[] argsIndex() default {};

}
