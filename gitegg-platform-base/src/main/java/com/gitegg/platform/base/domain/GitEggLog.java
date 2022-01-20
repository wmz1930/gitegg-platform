package com.gitegg.platform.base.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author GitEgg
 */
@Data
@ApiModel(description = "日志信息")
public class GitEggLog {

    @ApiModelProperty(value = "租户id")
    private Long tenantId;

    @ApiModelProperty(value = "接口名称")
    private String methodName;

    @ApiModelProperty(value = "请求的URI")
    protected String requestUri;

    @ApiModelProperty(value = "入参")
    private String inParams;

    @ApiModelProperty(value = "出参")
    private String outParams;

    @ApiModelProperty(value = "日志类型")
    private String logType;

    @ApiModelProperty(value = "操作名称")
    private String operationName;

    @ApiModelProperty(value = "操作的IP")
    private String operationIp;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "创建者")
    private Long creator;

}
