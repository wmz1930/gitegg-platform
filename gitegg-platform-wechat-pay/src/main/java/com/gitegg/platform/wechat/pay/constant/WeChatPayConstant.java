package com.gitegg.platform.wechat.pay.constant;

/**
 * @author GitEgg
 * @date 2023/7/17
 */
public class WeChatPayConstant {

    /**
     * Redis WX_PAY配置config key
     */
    public static final String WX_PAY_CONFIG_KEY = "wx:pay:config";

    /**
     * 当开启多租户模式时，Redis WX_PAY配置config key
     */
    public static final String WX_PAY_TENANT_CONFIG_KEY = "wx:pay:tenant:config:";

}
