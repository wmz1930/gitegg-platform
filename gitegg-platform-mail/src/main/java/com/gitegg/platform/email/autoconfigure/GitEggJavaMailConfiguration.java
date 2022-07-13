package com.gitegg.platform.email.autoconfigure;

import com.gitegg.platform.email.factory.JavaMailSenderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * <p>
 * GitEggJavaMail 自动装配类，扩展默认的MailProperties配置类，
 * </p>
 *
 * @author GitEgg
 */
@Slf4j
@Configuration
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GitEggJavaMailConfiguration {

    private final JavaMailSenderImpl javaMailSenderImpl;
    
    private final RedisTemplate redisTemplate;

    /**
     * 是否开启租户模式
     */
    @Value("${tenant.enable}")
    private Boolean enable;
    
    @Bean
    public JavaMailSenderFactory gitEggJavaMailSenderFactory() {
        return new JavaMailSenderFactory(redisTemplate, javaMailSenderImpl, enable);
    }
}
