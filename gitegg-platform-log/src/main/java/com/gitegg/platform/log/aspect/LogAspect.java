package com.gitegg.platform.log.aspect;

import com.gitegg.platform.base.annotation.log.AfterLog;
import com.gitegg.platform.base.annotation.log.AroundLog;
import com.gitegg.platform.base.annotation.log.BeforeLog;
import com.gitegg.platform.base.constant.LogLevelConstant;
import com.gitegg.platform.base.domain.GitEggLog;
import com.gitegg.platform.base.util.JsonUtils;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 
 * @ClassName: LogAspect
 * @Description:
 * @author GitEgg
 * @date 2019年4月27日 下午4:02:12
 *
 */
@Log4j2
@Aspect
@Component
public class LogAspect {

    /**
     * Before切点
     */
    @Pointcut("@annotation(com.gitegg.platform.base.annotation.log.BeforeLog)")
    public void beforeAspect() {
    }

    /**
     * After切点
     */
    @Pointcut("@annotation(com.gitegg.platform.base.annotation.log.AfterLog)")
    public void afterAspect() {
    }

    /**
     * Around切点
     */
    @Pointcut("@annotation(com.gitegg.platform.base.annotation.log.AroundLog)")
    public void aroundAspect() {
    }

    /**
     * 前置通知 记录用户的操作
     * 
     * @param joinPoint 切点
     */
    @Before("beforeAspect()")
    public void doBefore(JoinPoint joinPoint) {
        try {
            // 处理入参
            Object[] args = joinPoint.getArgs();
            StringBuffer inParams = new StringBuffer("");
            for (Object obj : args) {
                if (null != obj && !(obj instanceof ServletRequest) && !(obj instanceof ServletResponse)) {
                    String objJson = JsonUtils.objToJson(obj);
                    inParams.append(objJson);
                }
            }
            Method method = getMethod(joinPoint);
            String operationName = getBeforeLogName(method);
            addSysLog(joinPoint, String.valueOf(inParams), "BeforeLog", operationName);
        } catch (Exception e) {
            log.error("doBefore日志记录异常,异常信息:{}", e.getMessage());
        }
    }

    /**
     * 后置通知 记录用户的操作
     * 
     * @param joinPoint 切点
     */
    @AfterReturning(value = "afterAspect()", returning = "returnObj")
    public void doAfter(JoinPoint joinPoint, Object returnObj) {
        try {
            // 处理出参
            String outParams = JsonUtils.objToJson(returnObj);
            Method method = getMethod(joinPoint);
            String operationName = getAfterLogName(method);
            addSysLog(joinPoint, "AfterLog", outParams, operationName);
        } catch (Exception e) {
            log.error("doAfter日志记录异常,异常信息:{}", e.getMessage());
        }
    }

    /**
     * 前后通知 用于拦截记录用户的操作记录
     * 
     * @param joinPoint 切点
     * @throws Throwable
     */
    @Around("aroundAspect()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // 出参
        Object value = null;
        // 拦截的方法是否执行
        boolean execute = false;
        // 入参
        Object[] args = joinPoint.getArgs();
        try {
            // 处理入参
            StringBuffer inParams = new StringBuffer();
            for (Object obj : args) {
                if (null != obj && !(obj instanceof ServletRequest) && !(obj instanceof ServletResponse)) {
                    String objJson = JsonUtils.objToJson(obj);
                    inParams.append(objJson);
                }
            }
            execute = true;
            // 执行目标方法
            value = joinPoint.proceed(args);
            // 处理出参
            String outParams = JsonUtils.objToJson(value);
            Method method = getMethod(joinPoint);
            String operationName = getAroundLogName(method);
            // 记录日志
            addSysLog(joinPoint, String.valueOf(inParams), String.valueOf(outParams), operationName);
        } catch (Exception e) {
            log.error("around日志记录异常,异常信息:{}", e.getMessage());
            // 如果未执行则继续执行，日志异常不影响操作流程继续
            if (!execute) {
                value = joinPoint.proceed(args);
            }
            throw e;
        }
        return value;
    }

    /**
     * 日志入库 addSysLog(这里用一句话描述这个方法的作用)
     *
     * @Title: addSysLog
     * @Description:
     * @param joinPoint
     * @param inParams
     * @param outParams
     * @param operationName
     * @return void
     */
    @SneakyThrows
    public void addSysLog(JoinPoint joinPoint, String inParams, String outParams, String operationName) throws Exception {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            String ip = request.getRemoteAddr();
            GitEggLog gitEggLog = new GitEggLog();
            gitEggLog.setLogType("1");
            gitEggLog.setMethodName(joinPoint.getSignature().getName());
            gitEggLog.setInParams(String.valueOf(inParams));
            gitEggLog.setOutParams(String.valueOf(outParams));
            gitEggLog.setOperationIp(ip);
            gitEggLog.setOperationName(operationName);
            log.log(LogLevelConstant.OPERATION_LEVEL, LogLevelConstant.OPERATION_LEVEL_MESSAGE, JsonUtils.objToJson(gitEggLog));
        } catch (Exception e) {
            log.error("addSysLog日志记录异常,异常信息:{}", e.getMessage());
            throw e;
        }
    }

    /**
     * 获取注解中对方法的描述信息
     * 
     * @param joinPoint 切点
     * @return 方法描述
     * @throws Exception
     */
    public Method getMethod(JoinPoint joinPoint) throws Exception {
        String targetName = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] arguments = joinPoint.getArgs();
        Class<?> targetClass = Class.forName(targetName);
        Method[] methods = targetClass.getMethods();
        Method methodReturn = null;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class<?>[] clazzs = method.getParameterTypes();
                if (clazzs.length == arguments.length) {
                    methodReturn = method;
                    break;
                }
            }
        }
        return methodReturn;
    }

    /**
     * 
     * getBeforeLogName(获取before名称)
     *
     * @Title: getBeforeLogName
     * @Description:
     * @param method
     * @return String
     */
    public String getBeforeLogName(Method method) {
        String name = method.getAnnotation(BeforeLog.class).name();
        return name;
    }

    /**
     * 
     * getAfterLogName(获取after名称)
     *
     * @Title: getAfterLogName
     * @Description:
     * @param method
     * @return String
     */
    public String getAfterLogName(Method method) {
        String name = method.getAnnotation(AfterLog.class).name();
        return name;
    }

    /**
     * 
     * getAroundLogName(获取around名称)
     * @Title: getAroundLogName
     * @Description:
     * @param method
     * @return String
     *
     */
    public String getAroundLogName(Method method) {
        String name = method.getAnnotation(AroundLog.class).name();
        return name;
    }
}
