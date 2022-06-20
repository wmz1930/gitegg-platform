package com.gitegg.platform.justauth.autoconfigure;

import com.gitegg.platform.justauth.enums.HttpUtilTypeEnum;
import com.gitegg.platform.justauth.factory.GitEggAuthRequestFactory;
import com.xkcoding.http.HttpUtil;
import com.xkcoding.http.support.AbstractHttp;
import com.xkcoding.http.support.httpclient.HttpClientImpl;
import com.xkcoding.http.support.hutool.HutoolImpl;
import com.xkcoding.http.support.okhttp3.OkHttp3Impl;
import com.xkcoding.justauth.AuthRequestFactory;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * <p>
 * GitEggJustAuth 自动装配类，扩展默认的JustAuth配置类，
 * </p>
 *
 * @author GitEgg
 */
@Slf4j
@Configuration
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@EnableConfigurationProperties(JustAuthProperties.class)
public class GitEggJustAuthAutoConfiguration {

    private final AuthRequestFactory authRequestFactory;
    
    private final RedisTemplate redisTemplate;

    @Value("${justauth.http-util:default}")
    private String httpUtilType;
    
    @Bean
    @ConditionalOnProperty(prefix = "justauth", value = "enabled", havingValue = "true", matchIfMissing = true)
    public GitEggAuthRequestFactory gitEggAuthRequestFactory(JustAuthProperties justAuthProperties) {
        return new GitEggAuthRequestFactory(authRequestFactory, redisTemplate, justAuthProperties);
    }

    /**
     * JustAuth使用的http工具simple-http，默认启用http为：java11的httpClient 、OkHttp3、apache HttpClient、hutool-http
     * 当我们系统使用jdk8时，默认启用的是OkHttp3，但是这个版本和七牛、minio的sdk中使用的OkHttp3版本冲突，无法使用exclusions解决冲突，他们使用的版本差别太大，无法统一版本
     * 所以这里需要添加配置，根据配置选择使用的Http工具
     */
    @PostConstruct
    @ConditionalOnProperty(prefix = "justauth", value = "enabled", havingValue = "true", matchIfMissing = true)
    public void configHttpUtil() {
        if (StringUtils.isEmpty(httpUtilType))
        {
            log.info("config http util: default");
        }
        else
        {
            log.info("config http util: " + httpUtilType);
            if (HttpUtilTypeEnum.HUTOOL.type.equals(httpUtilType))
            {
                AbstractHttp http = new HutoolImpl();
                HttpUtil.setHttp(http);
            }
            else if (HttpUtilTypeEnum.APACHE.type.equals(httpUtilType))
            {
                AbstractHttp http = new HttpClientImpl();
                HttpUtil.setHttp(http);
            }
            else if (HttpUtilTypeEnum.OKHTTP3.type.equals(httpUtilType))
            {
                AbstractHttp http = new OkHttp3Impl();
                HttpUtil.setHttp(http);
            }
        }
    }
}
