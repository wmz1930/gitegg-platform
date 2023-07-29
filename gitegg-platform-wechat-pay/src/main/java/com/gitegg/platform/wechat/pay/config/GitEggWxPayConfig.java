package com.gitegg.platform.wechat.pay.config;

import com.github.binarywang.wxpay.config.WxPayConfig;

/**
 * @author GitEgg
 * @date 2023/7/29
 */
public class GitEggWxPayConfig extends WxPayConfig {

    protected String configKey;

    protected String tenantId;

    protected String md5;

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

}
