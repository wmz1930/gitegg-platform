package com.gitegg.platform.justauth.autoconfigure;

import com.gitegg.platform.justauth.factory.GitEggAuthRequestFactory;
import com.xkcoding.justauth.AuthRequestFactory;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

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
    
    @Bean
    @ConditionalOnProperty(prefix = "justauth", value = "enabled", havingValue = "true", matchIfMissing = true)
    public GitEggAuthRequestFactory gitEggAuthRequestFactory(JustAuthProperties justAuthProperties) {
        return new GitEggAuthRequestFactory(authRequestFactory, redisTemplate, justAuthProperties);
    }
}
