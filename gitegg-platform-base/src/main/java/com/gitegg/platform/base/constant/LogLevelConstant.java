package com.gitegg.platform.base.constant;

import org.apache.logging.log4j.Level;

/**
 * 自定义日志级别
 * 业务操作日志级别(级别越高，数字越小) off 0, fatal 100, error 200, warn 300, info 400, debug 500
 * warn operation api
 * @author GitEgg
 */
public class LogLevelConstant {

    /**
     * 操作日志
     */
    public static final Level OPERATION_LEVEL = Level.forName("OPERATION", 310);

    /**
     * 接口日志
     */
    public static final Level API_LEVEL = Level.forName("API", 320);

    /**
     * 操作日志信息
     */
    public static final String OPERATION_LEVEL_MESSAGE = "{type:'operation', content:{}}";

    /**
     * 接口日志信息
     */
    public static final String API_LEVEL_MESSAGE = "{type:'api', content:{}}";

}
