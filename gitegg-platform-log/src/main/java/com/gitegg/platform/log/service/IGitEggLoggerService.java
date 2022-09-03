package com.gitegg.platform.log.service;

import com.gitegg.platform.base.domain.GitEggLog;

/**
 * @author GitEgg
 * @date 2022/8/29
 */
public interface IGitEggLoggerService {

    /**
     * 新增日志
     * @param gitEggLog
     */
    void addLog(GitEggLog gitEggLog);

}
