package com.gitegg.platform.boot.util;

import cn.hutool.json.JSONUtil;
import com.gitegg.platform.base.constant.AuthConstant;
import com.gitegg.platform.base.domain.GitEggUser;
import com.gitegg.platform.boot.common.task.ThreadLocalRequestHeaderContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

/**
 * @author GitEgg
 */
@Slf4j
public class GitEggAuthUtils {

    /**
     * 获取用户信息
     *
     * @return GitEggUser
     */
    public static GitEggUser getCurrentUser() {

        HttpServletRequest request = GitEggWebUtils.getRequest();

        String user;

        if (null != request && !StringUtils.isEmpty(request.getHeader(AuthConstant.HEADER_USER)))
        {
            user = request.getHeader(AuthConstant.HEADER_USER);
        }
        else {
            //当request为null时，尝试从异步线程池的上下文变量中去取
            user = GitEggAuthUtils.getThreadPoolContextValue(AuthConstant.HEADER_USER);
        }

        if (StringUtils.isEmpty(user))
        {
            return null;
        }

        try {
            String userStr = URLDecoder.decode(user,"UTF-8");
            GitEggUser gitEggUser = JSONUtil.toBean(userStr, GitEggUser.class);
            return gitEggUser;
        } catch (UnsupportedEncodingException e) {
            log.error("获取当前登录用户失败:", e);
            return null;
        }

    }

    /**
     * 获取租户Id
     *
     * @return tenantId
     */
    public static String getTenantId() {
        HttpServletRequest request = GitEggWebUtils.getRequest();
        try {
            if (null != request && !StringUtils.isEmpty(request.getHeader(AuthConstant.TENANT_ID)))
            {
                return request.getHeader(AuthConstant.TENANT_ID);
            }
            else
            {
                //从当前登录用户取
                GitEggUser gitEggUser = GitEggAuthUtils.getCurrentUser();
                if (null != gitEggUser)
                {
                    return gitEggUser.getTenantId();
                }
                //当都为null时，尝试从异步线程池的上下文变量中去取
                String tenantId = GitEggAuthUtils.getThreadPoolContextValue(AuthConstant.TENANT_ID);
                return  tenantId;
            }
        } catch (Exception e) {
            log.error("获取当前租户失败:", e);
            return null;
        }
    }

    private static String getThreadPoolContextValue(String headerKey){
        Map<String, String> headers = ThreadLocalRequestHeaderContext.get();
        if (null != headers)
        {
            // CaseInsensitiveMap不区分大小写
            Map<String, String> headersMap = new CaseInsensitiveMap(headers);
            // request 的header大小写不敏感，在转换时全部转成了小写
            return headersMap.get(headerKey);
        }
        return null;
    }
}
