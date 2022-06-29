package com.gitegg.platform.email.constant;

/**
 * @author GitEgg
 * @date 2022/6/24
 */
public class JavaMailConstant {
    /**
     * Redis JavaMail配置config key
     */
    public static final String MAIL_CONFIG_KEY = "mail:config";

    /**
     * 当开启多租户模式时，Redis JavaMail配置config key
     */
    public static final String MAIL_TENANT_CONFIG_KEY = "mail:tenant:config:";
}
