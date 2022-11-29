package com.gitegg.platform.base.annotation.dict;

import java.lang.annotation.*;

/**
 * 数据字典注解，注解在方法上，自动设置返回参数的字典数据
 * 1、可以注解在 service和 controller上，只注解返回值，支持引用类型和常用的集合类型
 * 2、具体的实体类中，如果是引用类型，那么递归赋值
 * 3、支持的集合类型：List Set Queue ，引用类型：Array一维数组，普通对象类型（自定义实体bean）。
 * @author GitEgg
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DictAuto {
}
