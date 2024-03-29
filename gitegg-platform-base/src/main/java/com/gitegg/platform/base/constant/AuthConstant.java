package com.gitegg.platform.base.constant;

/**
 * @ClassName: AuthConstant
 * @Description: 鉴权常量类
 * @author GitEgg
 * @since 2021-10-12
 */
public class AuthConstant {

    /**
     * 租户id
     */
    public static final String TENANT_ID = "TenantId";

    /**
     * 默认租户ID
     */
    public static final Long DEFAULT_TENANT_ID = 0L;

    /**
     * JWT存储权限前缀
     */
    public static final String AUTHORITY_PREFIX = "ROLE_";

    /**
     * 账号锁定前缀
     */
    public static final String LOCK_ACCOUNT_PREFIX = "LOCK_ACCOUNT:";

    /**
     * JWT存储权限属性
     */
    public static final String AUTHORITY_CLAIM_NAME = "authorities";

    /**
     * 认证信息Http请求头
     */
    public static final String JWT_TOKEN_HEADER = "Authorization";

    /**
     * RefreshToken
     */
    public static final String REFRESH_TOKEN = "RefreshToken";

    /**
     * Basic认证前缀
     */
    public static final String JWT_TOKEN_PREFIX_BASIC = "Basic ";

    /**
     * Bearer认证 JWT令牌前缀
     */
    public static final String JWT_TOKEN_PREFIX = "Bearer ";

    /**
     * JWT载体key
     */
    public static final String JWT_PAYLOAD_KEY = "payload";

    /**
     * 请求头中的User
     */
    public static final String HEADER_USER = "User";

    /**
     * Redis缓存权限规则key
     */
    public static final String RESOURCE_ROLES_KEY = "auth:resource:roles";

    /**
     * 当开启多租户模式时，Redis缓存权限规则key
     */
    public static final String TENANT_RESOURCE_ROLES_KEY = "auth:tenant:resource:roles:";
    
    /**
     * Redis第三方登录 关闭多租户时配置map的key
     */
    public static final String SOCIAL_DEFAULT = "SocialDefault";
    
    /**
     * Redis第三方登录配置source key
     */
    public static final String SOCIAL_SOURCE_KEY = "social:source";
    
    /**
     * 当开启多租户模式时，Redis第三方登录配置source key
     */
    public static final String SOCIAL_TENANT_SOURCE_KEY = "social:tenant:source:";
    
    /**
     * Redis第三方登录配置config key
     */
    public static final String SOCIAL_CONFIG_KEY = "social:config";
    
    /**
     * 当开启多租户模式时，Redis第三方登录配置config key
     */
    public static final String SOCIAL_TENANT_CONFIG_KEY = "social:tenant:config:";

    /**
     * 黑名单token前缀
     */
    public static final String TOKEN_BLACKLIST = "auth:token:blacklist:";

    /**
     * 白名单token前缀
     */
    public static final String TOKEN_WHITELIST = "auth:token:whitelist:";

    /**
     * 密码加密方式
     */
    public static final String BCRYPT = "{bcrypt}";

    /**
     * 校验类型
     */
    public static final String GRANT_TYPE = "grant_type";

    /**
     * 客户端id
     */
    public static final String AUTH_CLIENT_ID = "client_id";
    
    /**
     * 第三方登录的校验码
     */
    public static final String SOCIAL_VALIDATION_PREFIX = "OAUTH:GRANT:SOCIAL:VALIDATION:CODE:";

    /**
     * 手机号 ,不再使用，统一使用TokenConstant中的 PHONE_NUMBER
     */
    @Deprecated
    public static final String PHONE_NUMBER = "phone_number";

    public static final String CLIENT_DETAILS_FIELDS = "client_id, client_secret, resource_ids, scope, "
            + "authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, "
            + "refresh_token_validity, additional_information, autoapprove";

    public static final String BASE_CLIENT_DETAILS_SQL = "select " + CLIENT_DETAILS_FIELDS + " from t_oauth_client_details";

    public static final String FIND_CLIENT_DETAILS_SQL = BASE_CLIENT_DETAILS_SQL + " where del_flag = 0 order by client_id";

    public static final String SELECT_CLIENT_DETAILS_SQL = BASE_CLIENT_DETAILS_SQL + " where del_flag = 0 and client_id = ?";
}
