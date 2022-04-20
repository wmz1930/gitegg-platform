package com.gitegg.platform.oauth2.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 公共鉴权url配置
 * @author GitEgg
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "oauth-list")
public class AuthUrlWhiteListProperties {

    /**
     * 静态文件白名单配置，不需要鉴权的url
     * 请求经过oauth2时，需要把服务名去掉，这里的配置oauth2不会去掉前缀
     */
    private List<String> staticFiles;

    /**
     * 白名单配置，不需要鉴权的url
     */
    private List<String> whiteUrls;

    /**
     * 需要鉴权的公共url配置，不需要单独给所有的角色增加的url
     */
    private List<String> authUrls;

    /**
     * 网关放行，由认证中心进行认证的url，比如单点登录
     */
    private List<String> tokenUrls;

}
