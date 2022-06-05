package com.gitegg.platform.justauth.enums;


/**
 * @ClassName: SourceTypeEnum
 * @Description: SourceTypeEnum
 * @author GitEgg
 * @date 2020年09月19日 下午11:49:45
 */
public enum SourceTypeEnum {

    /**
     * 默认
     */
    DEFAULT("default", "default"),

    /**
     * 自定义
     */
    CUSTOM("custom", "custom");
    
    public String key;

    public String value;

    SourceTypeEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
}
