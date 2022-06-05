package com.gitegg.platform.sms.service;

import cn.hutool.core.util.StrUtil;
import com.gitegg.platform.base.result.Result;
import com.gitegg.platform.sms.domain.SmsData;

import java.util.Collection;
import java.util.Collections;

/**
 * 短信发送接口
 * @author GitEgg
 */
public interface ISmsSendService {

    /**
     * 发送单个短信
     * @param smsData
     * @param phoneNumber
     * @return
     */
    default Result<?> sendSms(SmsData smsData, String phoneNumber){
        if (StrUtil.isEmpty(phoneNumber)) {
            return new Result();
        }
        return this.sendSms(smsData, Collections.singletonList(phoneNumber));
    }

    /**
     * 群发发送短信
     * @param smsData
     * @param phoneNumbers
     * @return
     */
    Result<?> sendSms(SmsData smsData, Collection<String> phoneNumbers);

}
