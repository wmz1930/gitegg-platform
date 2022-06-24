package com.gitegg.platform.justauth.factory;

import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.gitegg.platform.base.constant.AuthConstant;
import com.gitegg.platform.base.util.BeanCopierUtils;
import com.gitegg.platform.base.util.JsonUtils;
import com.gitegg.platform.boot.util.GitEggAuthUtils;
import com.gitegg.platform.justauth.cache.GitEggRedisStateCache;
import com.gitegg.platform.justauth.config.JustAuthConfig;
import com.gitegg.platform.justauth.config.JustAuthSource;
import com.gitegg.platform.justauth.enums.SourceTypeEnum;
import com.xkcoding.http.config.HttpConfig;
import com.xkcoding.justauth.AuthRequestFactory;
import com.xkcoding.justauth.autoconfigure.CacheProperties;
import com.xkcoding.justauth.autoconfigure.ExtendProperties;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.config.AuthDefaultSource;
import me.zhyd.oauth.config.AuthSource;
import me.zhyd.oauth.enums.AuthResponseStatus;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.request.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * GitEggAuthRequestFactory工厂类
 *
 * @author GitEgg
 */
@Slf4j
public class GitEggAuthRequestFactory {
    
    private RedisTemplate redisTemplate;
    
    private AuthRequestFactory authRequestFactory;
    
    private JustAuthProperties justAuthProperties;
    
    /**
     * 是否开启租户模式
     */
    private Boolean enable;

    public GitEggAuthRequestFactory(AuthRequestFactory authRequestFactory, RedisTemplate redisTemplate, JustAuthProperties justAuthProperties, Boolean enable) {
        this.authRequestFactory = authRequestFactory;
        this.redisTemplate = redisTemplate;
        this.justAuthProperties = justAuthProperties;
        this.enable = enable;
    }

    /**
     * 返回当前Oauth列表
     *
     * @return Oauth列表
     */
    public List<String> oauthList() {
        // 合并
        return authRequestFactory.oauthList();
    }

    /**
     * 返回AuthRequest对象
     *
     * @param source {@link AuthSource}
     * @return {@link AuthRequest}
     */
    public AuthRequest get(String source) {
        
        if (StrUtil.isBlank(source)) {
            throw new AuthException(AuthResponseStatus.NO_AUTH_SOURCE);
        }
    
        // 组装多租户的缓存配置key
        String authConfigKey = AuthConstant.SOCIAL_TENANT_CONFIG_KEY;
        if (enable) {
            authConfigKey += GitEggAuthUtils.getTenantId();
        } else {
            authConfigKey = AuthConstant.SOCIAL_CONFIG_KEY;
        }
    
        // 获取主配置，每个租户只有一个主配置
        String sourceConfigStr = (String) redisTemplate.opsForHash().get(authConfigKey, GitEggAuthUtils.getTenantId());
        AuthConfig authConfig = null;
        JustAuthSource justAuthSource = null;
        AuthRequest tenantIdAuthRequest = null;
        if (!StringUtils.isEmpty(sourceConfigStr))
        {
            try {
                // 转为系统配置对象
                JustAuthConfig justAuthConfig = JsonUtils.jsonToPojo(sourceConfigStr, JustAuthConfig.class);
                // 判断该配置是否开启了第三方登录
                if (justAuthConfig.getEnabled())
                {
                    // 根据配置生成StateCache
                    CacheProperties cacheProperties = new CacheProperties();
                    if (!StringUtils.isEmpty(justAuthConfig.getCacheType())
                            && !StringUtils.isEmpty(justAuthConfig.getCachePrefix())
                            && null != justAuthConfig.getCacheTimeout())
                    {
                        cacheProperties.setType(CacheProperties.CacheType.valueOf(justAuthConfig.getCacheType().toUpperCase()));
                        cacheProperties.setPrefix(justAuthConfig.getCachePrefix());
                        cacheProperties.setTimeout(Duration.ofMinutes(justAuthConfig.getCacheTimeout()));
                    }
                    else
                    {
                        cacheProperties = justAuthProperties.getCache();
                    }
                    
    
                    GitEggRedisStateCache gitEggRedisStateCache =
                            new GitEggRedisStateCache(redisTemplate, cacheProperties, enable);
                    
                    // 组装多租户的第三方配置信息key
                    String authSourceKey = AuthConstant.SOCIAL_TENANT_SOURCE_KEY;
                    if (enable) {
                        authSourceKey += GitEggAuthUtils.getTenantId();
                    } else {
                        authSourceKey = AuthConstant.SOCIAL_SOURCE_KEY;
                    }
                    // 获取具体的第三方配置信息
                    String sourceAuthStr = (String)redisTemplate.opsForHash().get(authSourceKey, source.toUpperCase());
                    if (!StringUtils.isEmpty(sourceAuthStr))
                    {
                        // 转为系统配置对象
                        justAuthSource = JsonUtils.jsonToPojo(sourceAuthStr, JustAuthSource.class);
                        authConfig = BeanCopierUtils.copyByClass(justAuthSource, AuthConfig.class);
                        // 组装scopes,因为系统配置的是逗号分割的字符串
                        if (!StringUtils.isEmpty(justAuthSource.getScopes()))
                        {
                            String[] scopes = justAuthSource.getScopes().split(StrUtil.COMMA);
                            authConfig.setScopes(Arrays.asList(scopes));
                        }
                        // 设置proxy
                        if (!StringUtils.isEmpty(justAuthSource.getProxyType()) && !StringUtils.isEmpty(justAuthSource.getProxyHostName())
                                && null != justAuthSource.getProxyPort())
                        {
                            HttpConfig httpConfig = HttpConfig.builder().proxy(new Proxy(Proxy.Type.valueOf(justAuthSource.getProxyType()), new InetSocketAddress(justAuthSource.getProxyHostName(), justAuthSource.getProxyPort()))).build();
                            if (null != justAuthConfig.getHttpTimeout())
                            {
                                httpConfig.setTimeout(justAuthConfig.getHttpTimeout());
                            }
                            authConfig.setHttpConfig(httpConfig);
                        }
                        // 组装好配置后，从配置生成request,判断是默认的第三方登录还是自定义第三方登录
                        if (SourceTypeEnum.DEFAULT.key.equals(justAuthSource.getSourceType()))
                        {
                            tenantIdAuthRequest = this.getDefaultRequest(source, authConfig, gitEggRedisStateCache);
                        }
                        else if (!StringUtils.isEmpty(justAuthConfig.getEnumClass()) && SourceTypeEnum.CUSTOM.key.equals(justAuthSource.getSourceType()))
                        {
                            try {
                                Class enumConfigClass = Class.forName(justAuthConfig.getEnumClass());
                                tenantIdAuthRequest = this.getExtendRequest(enumConfigClass, source, (ExtendProperties.ExtendRequestConfig) authConfig, gitEggRedisStateCache);
                            } catch (ClassNotFoundException e) {
                                log.error("初始化自定义第三方登录时发生异常:{}", e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("获取第三方登录时发生异常：{}", e);
            }
        }
        
        if (null == tenantIdAuthRequest)
        {
            tenantIdAuthRequest =  authRequestFactory.get(source);
        }

        return tenantIdAuthRequest;
    }
    
    /**
     * 获取单个的request
     * @param source
     * @return
     */
    private AuthRequest getDefaultRequest(String source, AuthConfig authConfig, GitEggRedisStateCache gitEggRedisStateCache) {
        AuthDefaultSource authDefaultSource;
        try {
            authDefaultSource = EnumUtil.fromString(AuthDefaultSource.class, source.toUpperCase());
        } catch (IllegalArgumentException var4) {
            return null;
        }
        
        // 从缓存获取租户单独配置
        switch(authDefaultSource) {
            case GITHUB:
                return new AuthGithubRequest(authConfig, gitEggRedisStateCache);
            case WEIBO:
                return new AuthWeiboRequest(authConfig, gitEggRedisStateCache);
            case GITEE:
                return new AuthGiteeRequest(authConfig, gitEggRedisStateCache);
            case DINGTALK:
                return new AuthDingTalkRequest(authConfig, gitEggRedisStateCache);
            case DINGTALK_ACCOUNT:
                return new AuthDingTalkAccountRequest(authConfig, gitEggRedisStateCache);
            case BAIDU:
                return new AuthBaiduRequest(authConfig, gitEggRedisStateCache);
            case CSDN:
                return new AuthCsdnRequest(authConfig, gitEggRedisStateCache);
            case CODING:
                return new AuthCodingRequest(authConfig, gitEggRedisStateCache);
            case OSCHINA:
                return new AuthOschinaRequest(authConfig, gitEggRedisStateCache);
            case ALIPAY:
                return new AuthAlipayRequest(authConfig, gitEggRedisStateCache);
            case QQ:
                return new AuthQqRequest(authConfig, gitEggRedisStateCache);
            case WECHAT_OPEN:
                return new AuthWeChatOpenRequest(authConfig, gitEggRedisStateCache);
            case WECHAT_MP:
                return new AuthWeChatMpRequest(authConfig, gitEggRedisStateCache);
            case WECHAT_ENTERPRISE:
                return new AuthWeChatEnterpriseQrcodeRequest(authConfig, gitEggRedisStateCache);
            case WECHAT_ENTERPRISE_WEB:
                return new AuthWeChatEnterpriseWebRequest(authConfig, gitEggRedisStateCache);
            case TAOBAO:
                return new AuthTaobaoRequest(authConfig, gitEggRedisStateCache);
            case GOOGLE:
                return new AuthGoogleRequest(authConfig, gitEggRedisStateCache);
            case FACEBOOK:
                return new AuthFacebookRequest(authConfig, gitEggRedisStateCache);
            case DOUYIN:
                return new AuthDouyinRequest(authConfig, gitEggRedisStateCache);
            case LINKEDIN:
                return new AuthLinkedinRequest(authConfig, gitEggRedisStateCache);
            case MICROSOFT:
                return new AuthMicrosoftRequest(authConfig, gitEggRedisStateCache);
            case MI:
                return new AuthMiRequest(authConfig, gitEggRedisStateCache);
            case TOUTIAO:
                return new AuthToutiaoRequest(authConfig, gitEggRedisStateCache);
            case TEAMBITION:
                return new AuthTeambitionRequest(authConfig, gitEggRedisStateCache);
            case RENREN:
                return new AuthRenrenRequest(authConfig, gitEggRedisStateCache);
            case PINTEREST:
                return new AuthPinterestRequest(authConfig, gitEggRedisStateCache);
            case STACK_OVERFLOW:
                return new AuthStackOverflowRequest(authConfig, gitEggRedisStateCache);
            case HUAWEI:
                return new AuthHuaweiRequest(authConfig, gitEggRedisStateCache);
            case GITLAB:
                return new AuthGitlabRequest(authConfig, gitEggRedisStateCache);
            case KUJIALE:
                return new AuthKujialeRequest(authConfig, gitEggRedisStateCache);
            case ELEME:
                return new AuthElemeRequest(authConfig, gitEggRedisStateCache);
            case MEITUAN:
                return new AuthMeituanRequest(authConfig, gitEggRedisStateCache);
            case TWITTER:
                return new AuthTwitterRequest(authConfig, gitEggRedisStateCache);
            case FEISHU:
                return new AuthFeishuRequest(authConfig, gitEggRedisStateCache);
            case JD:
                return new AuthJdRequest(authConfig, gitEggRedisStateCache);
            case ALIYUN:
                return new AuthAliyunRequest(authConfig, gitEggRedisStateCache);
            case XMLY:
                return new AuthXmlyRequest(authConfig, gitEggRedisStateCache);
            case AMAZON:
                return new AuthAmazonRequest(authConfig, gitEggRedisStateCache);
            case SLACK:
                return new AuthSlackRequest(authConfig, gitEggRedisStateCache);
            case LINE:
                return new AuthLineRequest(authConfig, gitEggRedisStateCache);
            case OKTA:
                return new AuthOktaRequest(authConfig, gitEggRedisStateCache);
            default:
                return null;
        }
    }
    
    
    private AuthRequest getExtendRequest(Class clazz, String source, ExtendProperties.ExtendRequestConfig extendRequestConfig, GitEggRedisStateCache gitEggRedisStateCache) {
        String upperSource = source.toUpperCase();
        try {
            EnumUtil.fromString(clazz, upperSource);
        } catch (IllegalArgumentException var8) {
            return null;
        }
        if (extendRequestConfig != null) {
            Class<? extends AuthRequest> requestClass = extendRequestConfig.getRequestClass();
            if (requestClass != null) {
                return (AuthRequest) ReflectUtil.newInstance(requestClass, new Object[]{extendRequestConfig, gitEggRedisStateCache});
            }
        }
        return null;
    }
}
