package com.gitegg.platform.base.constant;

import org.apache.logging.log4j.Level;

/**
 * 自定义日志级别
 * 业务操作日志级别(级别越高，数字越小) off 0, fatal 100, error 200, warn 300, info 400, debug 500
 * @author GitEgg
 */
public class LogLevelConstant {

    /**
     * 操作日志
     */
    public static final Level ACTION_LEVEL = Level.forName("ACTION", 310);

    /**
     * 接口日志
     */
    public static final Level VISIT_LEVEL = Level.forName("VISIT", 320);

    /**
     * 操作日志信息
     */
    public static final String ACTION_LEVEL_MESSAGE = "{type:'operation', content:{}}";

    /**
     * 接口日志信息
     */
    public static final String VISIT_LEVEL_MESSAGE = "{type:'visit', content:{}}";

}
