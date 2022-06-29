package com.gitegg.platform.email.impl;

import lombok.Data;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * @author GitEgg
 * @date 2022/6/24
 */
@Data
public class GitEggJavaMailSenderImpl extends JavaMailSenderImpl {

    /**
     * 配置id
     */
    private Long id;

    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 租户id
     */
    private String channelCode;

    /**
     * 配置的md5值
     */
    private String md5;

}
