package com.gitegg.platform.wechat.miniapp.config;

import cn.binarywang.wx.miniapp.config.impl.WxMaRedissonConfigImpl;
import lombok.NonNull;
import org.redisson.api.RedissonClient;

/**
 * @author GitEgg
 * @date 2023/7/21
 */
public class GitEggWxMaRedissonConfigImpl extends WxMaRedissonConfigImpl {

    protected String configKey;

    protected String tenantId;

    protected String md5;

    public GitEggWxMaRedissonConfigImpl(@NonNull RedissonClient redissonClient, String keyPrefix) {
        super(redissonClient, keyPrefix);
    }

    public GitEggWxMaRedissonConfigImpl(@NonNull RedissonClient redissonClient) {
        super(redissonClient);
    }

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
