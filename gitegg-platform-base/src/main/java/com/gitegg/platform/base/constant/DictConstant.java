package com.gitegg.platform.base.constant;

import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @ClassName: DictConstant
 * @Description: 数据字典常量类
 * @author GitEgg
 * @since 2021-10-12
 */
public class DictConstant {
    
    /**
     * 数据字典key
     */
    public static final String DICT_TENANT_MAP_PREFIX = "dict:tenant:";
    
    /**
     * 数据字典key
     */
    public static final String DICT_MAP_PREFIX = "dict";
    
    /**
     * 系统字典
     */
    public static final String DICT_SYSTEM_TYPE = ":system:";
    
    /**
     * 业务字典
     */
    public static final String DICT_BUSINESS_TYPE = ":business:";
    
    /**
     * 省市区
     */
    public static final String DICT_DISTRICT_TYPE = ":district:";
    
    /**
     * Name后缀，如果注解字段以Name结尾，并且注解中没有配置字典code的字段，那么去掉Name取字典值
     */
    public static final String NAME_SUFFIX = "Name";
    
    /**
     * 数据字典中，存储的所有字典的类型列表
     */
    public static final String ALL_DICT_TYPE = "ALL_DICT_TYPE";
    
    /**
     * 不进行处理的数据类型
     */
    public static final List<Class> baseTypeList = Lists.newArrayList(String.class, BigDecimal.class, Date.class, Integer.class, int.class, Double.class, double.class, Boolean.class, boolean.class, Long.class, long.class, Character.class, char.class);

}
