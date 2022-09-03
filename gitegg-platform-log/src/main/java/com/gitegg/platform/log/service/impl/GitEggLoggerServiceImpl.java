package com.gitegg.platform.log.service.impl;

import com.gitegg.platform.base.constant.LogLevelConstant;
import com.gitegg.platform.base.domain.GitEggLog;
import com.gitegg.platform.base.util.JsonUtils;
import com.gitegg.platform.log.service.IGitEggLoggerService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * @author GitEgg
 * @date 2022/8/29
 */
@Log4j2
@Component
public class GitEggLoggerServiceImpl implements IGitEggLoggerService {

    @Override
    public void addLog(GitEggLog gitEggLog) {

        try {
            log.log(LogLevelConstant.OPERATION_LEVEL, LogLevelConstant.OPERATION_LEVEL_MESSAGE, JsonUtils.objToJson(gitEggLog));
        } catch (Exception e) {
            log.error("记录日志时发生异常：{}", e);
        }
    }
}
