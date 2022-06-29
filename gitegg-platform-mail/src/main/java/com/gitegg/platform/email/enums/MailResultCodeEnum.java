package com.gitegg.platform.email.enums;


/**
 * @ClassName: MailResultCodeEnum
 * @Description: MailResultCodeEnum
 * @author GitEgg
 * @date 2020年09月19日 下午11:49:45
 */
public enum MailResultCodeEnum {

    /**
     * 默认
     */
    SUCCESS("success", "邮件发送成功"),

    /**
     * 自定义
     */
    ERROR("error", "邮件发送失败");

    public String code;

    public String message;

    MailResultCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
