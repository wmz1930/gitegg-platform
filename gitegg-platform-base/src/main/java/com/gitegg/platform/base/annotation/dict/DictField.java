package com.gitegg.platform.base.annotation.dict;

import java.lang.annotation.*;

/**
 * 数据字典注解，注解在字段上
 * 如果dictCode为空，且此字段是对象类型，那么表示此字段对象中拥有字典类型，
 * 也就是只有注解了此字段的数据才会去递归设置字典值，不去随便做无所谓的遍历
 *
 * @author GitEgg
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DictField {

    /**
     * 数据字典类型 ：系统字典 system 业务字典 business  地区字典 areas  其他字典：直接表名,例： t_sys_role
     * 1、确定选择哪种类型的数据字典
     */
    String dictType() default "business";
    
    /**
     * 数据字典编码，就是取哪些数据字典的值
     * 2、确定需要匹配数据字典的集合
     */
    String dictCode() default "";

    /**
     * 要最终转换最终数据字典的键，是实体类中的一个字段，通常配置为此字段的定义名称，通过此字段作为key来转换数据字典的值
     * 3、确定需要把实体中哪个字段转换为字典值
     */
    String dictKey() default "";

    /**
     * 如果是自定义表数据时，此字段作为字典code，对应数据表的字段
     * 4、表中作为数据字典的键
     */
    String dictFiled() default "";

    /**
     * 如果是自定义表数据时，此字段作为字典value，对应数据表的字段
     * 5、表中作为数据字典的值
     */
    String dictValue() default "";
}
