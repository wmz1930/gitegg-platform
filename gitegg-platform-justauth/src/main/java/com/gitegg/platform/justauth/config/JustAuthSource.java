package com.gitegg.platform.justauth.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * <p>
 * 租户第三方登录信息配置表
 * </p>
 *
 * @author GitEgg
 * @since 2022-05-19
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JustAuthSource {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 名称
     */
    private String sourceName;

    /**
     * 登录类型
     */
    private String sourceType;

    /**
     * 自定义Class
     */
    private String requestClass;

    /**
     * 客户端id
     */
    private String clientId;

    /**
     * 客户端Secret
     */
    private String clientSecret;

    /**
     * 回调地址
     */
    private String redirectUri;

    /**
     * 支付宝公钥
     */
    private String alipayPublicKey;

    /**
     * unionid
     */
    private Boolean unionId;

    /**
     * Stack Overflow Key
     */
    private String stackOverflowKey;

    /**
     * 企业微信网页应用ID
     */
    private String agentId;

    /**
     * 企业微信用户类型
     */
    private String userType;

    /**
     * DomainPrefix
     */
    private String domainPrefix;

    /**
     * 忽略校验code state
     */
    private Boolean ignoreCheckState;

    /**
     * 自定义授权scope
     */
    private String scopes;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 客户端操作系统类型
     */
    private Integer clientOsType;

    /**
     * 客户端包名
     */
    private String packId;

    /**
     * 开启PKC模式
     */
    private Boolean pkce;

    /**
     * Okta授权服务器的 ID
     */
    private String authServerId;

    /**
     * 忽略校验RedirectUri
     */
    private Boolean ignoreCheckRedirectUri;

    /**
     * Http代理类型
     */
    private String proxyType;

    /**
     * Http代理Host
     */
    private String proxyHostName;

    /**
     * Http代理Port
     */
    private Integer proxyPort;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
