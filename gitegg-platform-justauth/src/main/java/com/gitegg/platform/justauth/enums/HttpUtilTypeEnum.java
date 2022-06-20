package com.gitegg.platform.justauth.enums;


/**
 * @ClassName: HttpUtilTypeEnum
 * @Description: HttpUtilTypeEnum
 * @author GitEgg
 * @date 2020年09月19日 下午11:49:45
 */
public enum HttpUtilTypeEnum {

    /**
     * 默认
     */
    DEFAULT("default", "default"),

    /**
     * JAVA11
     */
    JAVA11("java11", "java.net.http.HttpClient"),

    /**
     * OKHTTP3
     */
    OKHTTP3("okhttp3", "okhttp3.OkHttpClient"),

    /**
     * APACHE
     */
    APACHE("apache", "org.apache.http.impl.client.HttpClients"),

    /**
     * HUTOOL
     */
    HUTOOL("hutool", "cn.hutool.http.HttpRequest");

    public String type;

    public String value;

    HttpUtilTypeEnum(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
