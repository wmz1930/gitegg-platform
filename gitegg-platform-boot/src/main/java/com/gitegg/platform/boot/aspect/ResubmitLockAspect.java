package com.gitegg.platform.boot.aspect;

import com.gitegg.platform.base.annotation.resubmit.ResubmitLock;
import com.gitegg.platform.base.enums.ResultCodeEnum;
import com.gitegg.platform.base.exception.SystemException;
import com.gitegg.platform.base.util.JsonUtils;
import com.gitegg.platform.boot.util.ExpressionUtils;
import com.gitegg.platform.boot.util.GitEggAuthUtils;
import com.gitegg.platform.boot.util.GitEggWebUtils;
import com.gitegg.platform.redis.lock.IDistributedLockService;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;


/**
 * @author GitEgg
 * @date 2022-4-10
 */
@Log4j2
@Component
@Aspect
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ConditionalOnProperty(name = "enabled", prefix = "resubmit-lock", havingValue = "true", matchIfMissing = true)
public class ResubmitLockAspect {

    private static final String REDIS_SEPARATOR = ":";

    private static final String RESUBMIT_CHECK_KEY_PREFIX = "resubmit_lock" + REDIS_SEPARATOR;

    private final IDistributedLockService distributedLockService;


    /**
     * 前置通知 防止重复提交
     *
     * @param joinPoint 切点
     * @param resubmitLock 注解配置
     */
    @Before("@annotation(resubmitLock)")
    public void resubmitCheck(JoinPoint joinPoint, ResubmitLock resubmitLock) throws Throwable {

        final Object[] args = joinPoint.getArgs();
        final String[] conditions = resubmitLock.conditions();

        //根据条件判断是否需要进行防重复提交检查
        if (ExpressionUtils.getConditionValue(args, conditions) && !ArrayUtils.isEmpty(args)) {
            doCheck(resubmitLock, args);
        }
    }

    /**
     * key的组成为: resubmit_lock:userId:sessionId:uri:method:(根据spring EL表达式对参数进行拼接)
     *
     * @param resubmitLock 注解
     * @param args       方法入参
     */
    private void doCheck(@NonNull ResubmitLock resubmitLock, Object[] args) {

        final String[] keys = resubmitLock.keys();
        final boolean currentUser = resubmitLock.currentUser();
        final boolean currentSession = resubmitLock.currentSession();

        String method = GitEggWebUtils.getRequest().getMethod();
        String uri = GitEggWebUtils.getRequest().getRequestURI();

        StringBuffer lockKeyBuffer = new StringBuffer(RESUBMIT_CHECK_KEY_PREFIX);

        if (null != GitEggAuthUtils.getTenantId())
        {
            lockKeyBuffer.append( GitEggAuthUtils.getTenantId()).append(REDIS_SEPARATOR);
        }

        // 此判断暂时预留，适配后续无用户登录场景，因部分浏览器在跨域请求时，不允许request请求携带cookie，导致每次sessionId都是新的，所以这里默认使用用户id作为key的一部分，不使用sessionId
        if (currentSession)
        {
            lockKeyBuffer.append( GitEggWebUtils.getSessionId()).append(REDIS_SEPARATOR);
        }

        // 默认没有将user数据作为防重key
        if (currentUser && null != GitEggAuthUtils.getCurrentUser())
        {
            lockKeyBuffer.append( GitEggAuthUtils.getCurrentUser().getId() ).append(REDIS_SEPARATOR);
        }

        lockKeyBuffer.append(uri).append(REDIS_SEPARATOR).append(method);

        StringBuffer parametersBuffer = new StringBuffer();
        // 优先判断是否设置防重字段，因keys试数组，取值时是按照顺序排列的，这里不需要重新排序
        if (ArrayUtils.isNotEmpty(keys))
        {
            Object[] argsForKey = ExpressionUtils.getExpressionValue(args, keys);
            for (Object obj : argsForKey) {
                parametersBuffer.append(REDIS_SEPARATOR).append(String.valueOf(obj));
            }
        }
        // 如果没有设置防重的字段，那么需要把所有的字段和值作为key，因通过反射获取字段时，顺序时不确定的，这里取出来之后需要进行排序
        else {
            // 只有当keys为空时，ignoreKeys和argsIndex生效
            final String[] ignoreKeys = resubmitLock.ignoreKeys();
            final int[] argsIndex = resubmitLock.argsIndex();
            if (ArrayUtils.isNotEmpty(argsIndex))
            {
                for(int index : argsIndex) {
                    parametersBuffer.append(REDIS_SEPARATOR).append( getKeyAndValueJsonStr(args[index], ignoreKeys));
                }
            }
            else
            {
                for(Object obj : args) {
                    parametersBuffer.append(REDIS_SEPARATOR).append( getKeyAndValueJsonStr(obj, ignoreKeys) );
                }
            }
        }

        // 将请求参数取md5值作为key的一部分，MD5理论上会重复，但是key中还包含session或者用户id，所以同用户在极端时间内请参数不同生成的相同md5值的概率极低
        String parametersKey = DigestUtils.md5DigestAsHex(parametersBuffer.toString().getBytes());
        lockKeyBuffer.append(parametersKey);

        try {
            boolean isLock = distributedLockService.tryLock(lockKeyBuffer.toString(), 0, resubmitLock.interval(), resubmitLock.timeUnit());
            if (!isLock){
                throw new SystemException(ResultCodeEnum.RESUBMIT_LOCK.code, ResultCodeEnum.RESUBMIT_LOCK.msg);
            }
        } catch (InterruptedException e) {
            throw new SystemException(ResultCodeEnum.RESUBMIT_LOCK.code, ResultCodeEnum.RESUBMIT_LOCK.msg);
        }
    }

    /**
     * 将字段转换为json字符串
     * @param obj
     * @return
     */
    public static String getKeyAndValueJsonStr(Object obj, String[] ignoreKeys) {
        Map<String, Object> map = Maps.newHashMap();
        // 得到类对象
        Class objCla = (Class) obj.getClass();
        /* 得到类中的所有属性集合 */
        Field[] fs = objCla.getDeclaredFields();
        for (int i = 0; i < fs.length; i++) {
            Field f = fs[i];
            // 设置些属性是可以访问的
            f.setAccessible(true);
            Object val = new Object();
            try {
                String filedName = f.getName();
                // 如果字段在排除列表，那么不将字段放入map
                if (null != ignoreKeys && Arrays.asList(ignoreKeys).contains(filedName)){
                    continue;
                }
                val = f.get(obj);
                // 得到此属性的值
                // 设置键值
                map.put(filedName, val);
            } catch (IllegalArgumentException e) {
                log.error("getKeyAndValue IllegalArgumentException", e);
                throw new RuntimeException("您的操作太频繁，请稍后再试");
            } catch (IllegalAccessException e) {
                log.error("getKeyAndValue IllegalAccessException", e);
                throw new RuntimeException("您的操作太频繁，请稍后再试");
            }
        }
        Map<String, Object> sortMap = sortMapByKey(map);
        String mapStr = JsonUtils.mapToJson(sortMap);
        return mapStr;
    }

    private static Map<String, Object> sortMapByKey(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<String, Object> sortMap = new TreeMap<String, Object>(new Comparator<String>() {
            @Override
            public int compare(String o1,String o2) {
                return ((String)o1).compareTo((String) o2);
            }
        });
        sortMap.putAll(map);
        return sortMap;
    }

}
