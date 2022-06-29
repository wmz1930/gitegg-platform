package com.gitegg.platform.email.factory;

import com.gitegg.platform.base.constant.GitEggConstant;
import com.gitegg.platform.base.exception.BusinessException;
import com.gitegg.platform.base.util.JsonUtils;
import com.gitegg.platform.boot.util.GitEggAuthUtils;
import com.gitegg.platform.email.constant.JavaMailConstant;
import com.gitegg.platform.email.impl.GitEggJavaMailSenderImpl;
import com.gitegg.platform.email.props.GitEggMailProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮件发送工厂类
 * @author GitEgg
 * @date 2022/6/23
 */
@Slf4j
public class JavaMailSenderFactory {

    private RedisTemplate redisTemplate;

    private JavaMailSenderImpl javaMailSenderImpl;

    /**
     * 是否开启租户模式
     */
    private Boolean enable;

    /**
     * JavaMailSender 缓存
     * 尽管存在多个微服务，但是只需要在每个微服务初始化一次即可
     */
    private final static Map<String, GitEggJavaMailSenderImpl> javaMailSenderMap = new ConcurrentHashMap<>();

    public JavaMailSenderFactory(RedisTemplate redisTemplate, JavaMailSenderImpl javaMailSenderImpl, Boolean enable) {
        this.redisTemplate = redisTemplate;
        this.javaMailSenderImpl = javaMailSenderImpl;
        this.enable = enable;
    }

    /**
     * 指定邮件发送渠道
     * @return
     */
    public JavaMailSenderImpl getMailSender(String... channelCode){
        if (null == channelCode || channelCode.length == GitEggConstant.COUNT_ZERO
                || null == channelCode[GitEggConstant.Number.ZERO])
        {
            return this.getDefaultMailSender();
        }
        // 首先判断是否开启多租户
        String mailConfigKey = JavaMailConstant.MAIL_TENANT_CONFIG_KEY;

        if (enable) {
            mailConfigKey += GitEggAuthUtils.getTenantId();
        } else {
            mailConfigKey = JavaMailConstant.MAIL_CONFIG_KEY;
        }

        // 从缓存获取邮件配置信息
        // 根据channel code获取配置，用channel code时，不区分是否是默认配置
        String propertiesStr = (String) redisTemplate.opsForHash().get(mailConfigKey, channelCode[GitEggConstant.Number.ZERO]);
        if (StringUtils.isEmpty(propertiesStr))
        {
            throw new BusinessException("未获取到[" + channelCode[GitEggConstant.Number.ZERO] + "]的邮件配置信息");
        }
        GitEggMailProperties properties = null;
        try {
            properties = JsonUtils.jsonToPojo(propertiesStr, GitEggMailProperties.class);
        } catch (Exception e) {
            log.error("转换邮件配置信息异常:{}", e);
            throw new BusinessException("转换邮件配置信息异常:" + e);
        }
        return this.getMailSender(mailConfigKey, properties);
    }

    /**
     * 不指定邮件发送渠道，取默认配置
     * @return
     */
    public JavaMailSenderImpl getDefaultMailSender(){
        // 首先判断是否开启多租户
        String mailConfigKey = JavaMailConstant.MAIL_TENANT_CONFIG_KEY;

        if (enable) {
            mailConfigKey += GitEggAuthUtils.getTenantId();
        } else {
            mailConfigKey = JavaMailConstant.MAIL_CONFIG_KEY;
        }

        // 获取所有邮件配置列表
        Map<Object, Object> propertiesMap = redisTemplate.opsForHash().entries(mailConfigKey);
        Iterator<Map.Entry<Object, Object>> entries = propertiesMap.entrySet().iterator();
        // 如果没有设置取哪个配置，那么获取默认的配置
        GitEggMailProperties properties = null;
        try {
            while (entries.hasNext()) {
                Map.Entry<Object, Object> entry = entries.next();
                // 转为系统配置对象
                GitEggMailProperties propertiesEnable = JsonUtils.jsonToPojo((String) entry.getValue(), GitEggMailProperties.class);
                if (propertiesEnable.getChannelStatus().intValue() == GitEggConstant.ENABLE) {
                    properties = propertiesEnable;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.getMailSender(mailConfigKey, properties);
    }

    private JavaMailSenderImpl getMailSender(String mailConfigKey, GitEggMailProperties properties) {
        // 根据最新配置信息判断是否从本地获取mailSender，在配置保存时，计算实体配置的md5值，然后进行比较，不要在每次对比的时候进行md5计算
        if (null != properties && !StringUtils.isEmpty(properties.getMd5()))
        {
            GitEggJavaMailSenderImpl javaMailSender = javaMailSenderMap.get(mailConfigKey);
            if (null == javaMailSender || !properties.getMd5().equals(javaMailSender.getMd5()))
            {
                // 如果没有配置信息，那么直接返回系统默认配置的mailSender
                javaMailSender = new GitEggJavaMailSenderImpl();
                this.applyProperties(properties, javaMailSender);
                javaMailSender.setMd5(properties.getMd5());
                javaMailSender.setId(properties.getId());
                // 将MailSender放入缓存
                javaMailSenderMap.put(mailConfigKey, javaMailSender);
            }
            return javaMailSender;
        }
        else
        {
            return this.javaMailSenderImpl;
        }
    }

    private void applyProperties(MailProperties properties, JavaMailSenderImpl sender) {
        sender.setHost(properties.getHost());
        if (properties.getPort() != null) {
            sender.setPort(properties.getPort());
        }

        sender.setUsername(properties.getUsername());
        sender.setPassword(properties.getPassword());
        sender.setProtocol(properties.getProtocol());
        if (properties.getDefaultEncoding() != null) {
            sender.setDefaultEncoding(properties.getDefaultEncoding().name());
        }

        if (!properties.getProperties().isEmpty()) {
            sender.setJavaMailProperties(this.asProperties(properties.getProperties()));
        }

    }

    private Properties asProperties(Map<String, String> source) {
        Properties properties = new Properties();
        properties.putAll(source);
        return properties;
    }
}
