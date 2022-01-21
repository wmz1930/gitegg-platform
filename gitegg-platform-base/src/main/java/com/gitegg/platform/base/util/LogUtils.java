package com.gitegg.platform.base.util;

import com.gitegg.platform.base.constant.LogLevelConstant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author GitEgg
 */
public class LogUtils {

    public static Logger logger = LogManager.getLogger(LogUtils.class);

    /**
     * 记录操作日志
     *
     * @param message
     * @param params
     * @return
     * @throws Exception
     */
    public static void action(String message, Object... params) {
        logger.log(LogLevelConstant.ACTION_LEVEL, message, params);
    }

    /**
     * 记录访问日志
     *
     * @param message
     * @param params
     * @return
     * @throws Exception
     */
    public static void visit( String message, Object... params) {
        logger.log(LogLevelConstant.VISIT_LEVEL, message, params);
    }

}
