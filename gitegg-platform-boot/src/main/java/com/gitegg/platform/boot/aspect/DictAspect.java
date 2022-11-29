package com.gitegg.platform.boot.aspect;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gitegg.platform.base.annotation.dict.DictAuto;
import com.gitegg.platform.base.annotation.dict.DictField;
import com.gitegg.platform.base.constant.DictConstant;
import com.gitegg.platform.base.constant.GitEggConstant;
import com.gitegg.platform.base.result.Result;
import com.gitegg.platform.boot.util.GitEggAuthUtils;
import com.gitegg.platform.redis.lock.IDistributedLockService;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import jodd.util.StringPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;


/**
 * 数据字典切面
 * @author GitEgg
 * @date 2022-4-10
 */
@Log4j2
@Component
@Aspect
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@ConditionalOnProperty(name = "enabled", prefix = "dict", havingValue = "true", matchIfMissing = true)
public class DictAspect {
    
    /**
     * 是否开启租户模式
     */
    @Value("${tenant.enable}")
    private Boolean enable;
    
    private final RedisTemplate redisTemplate;
    
    /**
     * 后置通知 解析返回参数，进行字典设置
     * @AfterReturning 只有存在返回值时才会执行 @After 无论有没有返回值都会执行  所以这里使用 @AfterReturning 只有存在返回值时才执行字典值注入操作
     * @param dictAuto 注解配置
     */
    @AfterReturning(pointcut = "@annotation(dictAuto)", returning = "returnObj")
    public void doAfterReturning( DictAuto dictAuto, Object returnObj){
        // 返回不为null时，进行数据字典处理
        if (null != returnObj) {
            doDictAuto(dictAuto, returnObj);
        }
    }

    /**
     * key的组成为: dict:userId:sessionId:uri:method:(根据spring EL表达式对参数进行拼接)
     * 此处要考虑多种返回类型，集合类型、引用类型、对象类型和基本数据类型，这里只处理 集合类型：List Set Queue ，引用类型：Array数组，Array只支持一维数组。
     * 对于对象中的子对象，为了提升性能，同样需要加@DictField注解才去填充，否则每个子对象都去递归判断，影响性能
     * 我们要考虑此处的逻辑：
     * 1、判断返回数据类型，如果是集合类型，那么取出包含实体对象的集合类，然后进行对象解析
     * 2、如果是对象类型，那么直接进行对象解析
     * 3、如果是IPage类型，那么取出其中的list数据，判断是否为空，不为空，执行 1 步骤
     * 4、如果是Result类型，判断Result的data是IPage还是集合类型，分别执行对应的 1 步骤 或 3 步骤，如果不是IPage也不是集合类型，直接执行第 2 步骤
     * @param dictAuto 注解
     * @param objectReturn 方法返回值
     */
    private void doDictAuto(@NonNull DictAuto dictAuto, Object objectReturn) {
       // 临时存储数据字典map
       Map<String, Map<Object, Object>> dictMap = new HashMap<>();
       this.translationObjectDict(objectReturn, dictMap);
    }
    
    /**
     * 找到实际的对象或对象列表
     * 此处要考虑多种返回类型，集合类型、引用类型、对象类型和基本数据类型，这里只处理 集合类型：List Set Queue ，引用类型：Array一维数组。
     * @param objectReturn
     * @param dictMap
     * @return
     */
    private void translationObjectDict(Object objectReturn, Map<String, Map<Object, Object>> dictMap) {
        if (Objects.isNull(objectReturn))
        {
            return;
        }
        // 判断返回值类型是Result、IPage、List、Object
        if (objectReturn instanceof Result) {
            Object objectTarget = ((Result) objectReturn).getData();
            translationObjectDict(objectTarget, dictMap);
        } else if (objectReturn instanceof IPage) {
            List<Object> objectTargetList = ((IPage) objectReturn).getRecords();
            translationObjectDict(objectTargetList, dictMap);
        } else if (objectReturn instanceof Collection) {
            ((Collection) objectReturn).forEach(object-> translationObjectDict(object, dictMap));
        } else if (ArrayUtil.isArray(objectReturn)) {
            // 数组这里需要处理
            ((Collection) objectReturn).forEach(object-> translationObjectDict(object, dictMap));
        } else {
            parseObjectFieldCodeValue(objectReturn, dictMap);
        }
    }
    
    /**
     * 取出对象中需要进行字典转换的字段
     *
     * @param targetObj   : 取字段的对象
     * @param dictMap     : 存储数据字典
     * @author liam
     */
    private void parseObjectFieldCodeValue(Object targetObj, Map<String, Map<Object, Object>> dictMap) {
        if (Objects.isNull(targetObj))
        {
            return;
        }
        // 获取当前对象所有的field
        Field[] declaredFields = targetObj.getClass().getDeclaredFields();
        // 构造填充映射关系
        Arrays.stream(declaredFields).forEach(field ->
            // 递归处理
            parseFieldObjDict(field, targetObj,
                    fieldObj -> parseObjectFieldCodeValue(fieldObj, dictMap),
                    // 解析注解字段信息
                    () -> parseDictAnnotation(targetObj, field, dictMap)
            )
        );
    }
    
    /**
     * 解析field对象，对基本数据类型和复杂类型直接根据注解赋值，对于对象或集合类型，继续进行递归遍历
     *
     * @param field          : 字段对象
     * @param obj            : 字段所属的obj对象
     * @param recursiveFunc  : 递归处理方法
     * @author liam
     */
    private static void parseFieldObjDict(Field field, Object obj, Consumer<Object> recursiveFunc,
                                          NestedFunction parseAnnotationFunc) {
        Class cls = field.getType();
        // 不处理map数据
        if (Map.class.isAssignableFrom(cls)) {
            return;
        }
        // 需要数据字典转换的属性：有Dict注解， @DictField只注解在普通字段上，不要注解到复杂对象上
        if (field.isAnnotationPresent(DictField.class)) {
            // 分析注解并转换数据字典值
            parseAnnotationFunc.run();
        }
        // 没有注解的属性判断
        else {
            try {
                // 获取字段值且非空处理
                field.setAccessible(true);
                Optional.ofNullable(field.get(obj)).ifPresent(fieldValue -> {
                            // 集合类型，如果泛型的类型是JavaBean，继续递归处理
                            if (Collection.class.isAssignableFrom(cls)) {
                                // 如果是list-map结果，则这里返回null
                                Class generic = getGeneric(obj.getClass(), field.getName());
                                if (null != generic && notInFilterClass(generic)) {
                                    // 循环递归处理
                                    ((Collection) fieldValue).forEach(recursiveFunc::accept);
                                }
                            }
                            // 非基本数据类型
                            else if (notInFilterClass(cls)) {
                                recursiveFunc.accept(fieldValue);
                            }
                        }
                );
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    /**
     * 获取一个类的属性的泛型；如果没有泛型，则返回null；
     * P.s 如果有多个，取第一个；如果有多层泛型，也返回null，比如List<Map>
     *
     * @param cls      :
     * @param property : 属性名
     * @author liam
     */
    public static Class getGeneric(Class cls, String property) {
        try {
            Type genericType = cls.getDeclaredField(property).getGenericType();
            // 如果是泛型参数的类型
            if (null != genericType && genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                Type type = pt.getActualTypeArguments()[GitEggConstant.Number.ZERO];
                // 这里，type也可能是 ParameterizedType， 直接不考虑
                if (type instanceof Class) {
                    return (Class) type;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 解析含有注解@DictField并赋值
     *
     * @param obj         : 对象
     * @param field       : 字段
     * @param dictMap     : 数据字典
     * @author liam
     */
    private void parseDictAnnotation(Object obj, Field field, Map<String, Map<Object, Object>> dictMap) {
        // 读取注解信息，获取编码类型
        DictField dictField = field.getAnnotation(DictField.class);
        String fieldName = field.getName();
        // 根据Dict的codeName属性或者字段名称，获取字典编码code
        String code = getFieldValue(obj, dictField, fieldName);
        if (!Strings.isNullOrEmpty(code)) {
            String dictType = dictField.dictType();
            String dictCode = dictField.dictCode();
            String dictKey = dictType + StringPool.COLON + dictCode;
            // 首先判断是否开启多租户
            String redisDictKey = DictConstant.DICT_TENANT_MAP_PREFIX;
    
            if (enable) {
                redisDictKey += GitEggAuthUtils.getTenantId() + StringPool.COLON + dictKey;
            } else {
                redisDictKey = DictConstant.DICT_MAP_PREFIX + dictKey;
            }
            
            Map<Object, Object> dictKeyValue = dictMap.get(redisDictKey);
            // 首先从dictMap中获取值，如果没有，再从Redis数据库中获取值
            if (null == dictKeyValue) {
                // 从Redis数据库获取值
                Map<Object, Object> dictCodeMap = redisTemplate.opsForHash().entries(redisDictKey);
                dictMap.put(redisDictKey, dictCodeMap);
            }
            if (null != dictKeyValue.get(code))
            {
                try {
                    // 给Field赋值最终的数据字典
                    field.setAccessible(true);
                    field.set(obj, dictKeyValue.get(code));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
    
    /**
     * 根据Dict的codeName属性或者字段名称，获取字段值
     * 注意：如果当前字段没有以Name结尾，那就取当前字段的值；也就是根据当前字段的值转换。
     *
     * @param obj            : 对象
     * @param dictField      : 字段注解对象
     * @param fieldName      : 字段名称
     * @return java.lang.String
     * @author liam
     */
    private String getFieldValue(Object obj, DictField dictField, String fieldName) {
        String codeName = dictField.dictKey();
        if (Strings.isNullOrEmpty(codeName)) {
            // 如果当前字段是Name结尾，进行截取；否则取当前字段名称
            int endNameIndex = fieldName.lastIndexOf(DictConstant.NAME_SUFFIX);
            if (endNameIndex != -1) {
                codeName = fieldName.substring(0, endNameIndex);
            } else {
                codeName = fieldName;
            }
        }
        return getPropertyValue(obj, codeName);
    }
    
    /**
     * 获取对象里指定属性的值，并转化为字符串
     *
     * @param obj          : 对象
     * @param propertyName : 对象里面的属性名称
     * @author liam
     */
    private String getPropertyValue(Object obj, String propertyName) {
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(obj);
        if (beanWrapper.isReadableProperty(propertyName)) {
            Object propertyValue = beanWrapper.getPropertyValue(propertyName);
            if (null != propertyValue) {
                return propertyValue.toString();
            }
        }
        return "";
    }
    
    /**
     * 判断不在过滤类(常用基本数据类型)中
     */
    private static boolean notInFilterClass(Class cls) {
        return !DictConstant.baseTypeList.contains(cls);
    }
    
    /**
     * 函数式接口：类似freemarker中的<#nested>处理
     */
    @FunctionalInterface
    public interface NestedFunction {
        /**
         * 无参无返回值的方法运行
         */
        void run();
    }

}
