package com.gitegg.platform.email.props;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.boot.autoconfigure.mail.MailProperties;

/**
 * @author GitEgg
 * @date 2022/6/24
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitEggMailProperties extends MailProperties {

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
     * 状态
     */
    private Integer channelStatus;

    /**
     * 配置的md5值
     */
    private String md5;
}
