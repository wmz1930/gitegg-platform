package com.gitegg.platform.boot.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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

    /**
     * 获取所有的 header
     * @param request
     * @return
     */
    public static Map<String, String> getHeaders(@NotNull HttpServletRequest request) {
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String name	= enumeration.nextElement();
            String value = request.getHeader(name);
            headerMap.put(name, value);
        }
        return headerMap;
    }

    /**
     * 获取所有的参数
     * @param request
     * @return
     */
    public static Map<String, String> getParameters(@NotNull HttpServletRequest request) {
        Map<String, String> parameterMap = new HashMap<>();
        Enumeration<String> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String name	= enumeration.nextElement();
            String value = request.getParameter(name);
            parameterMap.put(name, value);
        }
        return parameterMap;
    }

}
