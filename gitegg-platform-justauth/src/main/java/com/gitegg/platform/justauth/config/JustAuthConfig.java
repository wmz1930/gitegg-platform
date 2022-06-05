package com.gitegg.platform.justauth.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * <p>
 * 租户第三方登录功能配置表
 * </p>
 *
 * @author GitEgg
 * @since 2022-05-16
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JustAuthConfig {

    private static final long serialVersionUID = 1L;
    
    /**
     * 主键
     */
    private Long id;
    
    /**
     * 登录开关
     */
    private Boolean enabled;
    
    /**
     * 配置类
     */
    private String enumClass;
    
    /**
     * Http超时
     */
    private Integer httpTimeout;
    
    /**
     * 缓存类型
     */
    private String cacheType;
    
    /**
     * 缓存前缀
     */
    private String cachePrefix;
    
    /**
     * 缓存超时
     */
    private Integer cacheTimeout;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
}
