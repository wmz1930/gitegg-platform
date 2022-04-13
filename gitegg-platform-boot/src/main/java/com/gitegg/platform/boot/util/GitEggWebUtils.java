package com.gitegg.platform.boot.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author GitEgg
 */
public class GitEggWebUtils extends WebUtils {

    /**
     * 获取 HttpServletRequest
     *
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        return (requestAttributes == null) ? null : ((ServletRequestAttributes) requestAttributes).getRequest();
    }

    /**
     * 获取 SessionId
     *
     */
    public static String getSessionId() {
        HttpServletRequest request = getRequest();
        if (null != request && null != request.getSession(false))
        {
            return request.getSession(false).getId();
        }else{
            return null;
        }
    }

}
