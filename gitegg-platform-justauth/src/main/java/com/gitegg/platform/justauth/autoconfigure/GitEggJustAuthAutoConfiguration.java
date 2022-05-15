package com.gitegg.platform.justauth.autoconfigure;

import com.gitegg.platform.justauth.factory.GitEggAuthRequestFactory;
import com.xkcoding.justauth.AuthRequestFactory;
import com.xkcoding.justauth.autoconfigure.JustAuthAutoConfiguration;
import com.xkcoding.justauth.autoconfigure.JustAuthProperties;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.cache.AuthStateCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * GitEggJustAuth 自动装配类，扩展默认的JustAuth配置类，
 * </p>
 *
 * @author GitEgg
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(JustAuthProperties.class)
public class GitEggJustAuthAutoConfiguration extends JustAuthAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "justauth", value = "enabled", havingValue = "true", matchIfMissing = true)
    @Override
    public AuthRequestFactory authRequestFactory(JustAuthProperties properties, @Qualifier("authStateCache") AuthStateCache authStateCache) {
        return new GitEggAuthRequestFactory(properties, authStateCache);
    }

}
