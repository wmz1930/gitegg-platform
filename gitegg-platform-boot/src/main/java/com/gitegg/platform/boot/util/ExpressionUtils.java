package com.gitegg.platform.boot.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring Expression 工具类
 * @author GitEgg
 * @date 2022-4-11
 */
public class ExpressionUtils {

    private static final Map<String, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>(64);

    /**
     * 获取Expression对象
     *
     * @param expressionString Spring EL 表达式字符串 例如 #{param.id}
     * @return Expression
     */
    @Nullable
    public static Expression getExpression(@Nullable String expressionString) {
        if (StringUtils.isBlank(expressionString)) {
            return null;
        }
        if (EXPRESSION_CACHE.containsKey(expressionString)) {
            return EXPRESSION_CACHE.get(expressionString);
        }
        Expression expression = new SpelExpressionParser().parseExpression(expressionString);
        EXPRESSION_CACHE.put(expressionString, expression);
        return expression;
    }

    /**
     * 根据Spring EL表达式字符串从根对象中求值
     *
     * @param root             根对象
     * @param expressionString Spring EL表达式
     * @param clazz            值得类型
     * @param <T>              泛型
     * @return 值
     */
    @Nullable
    public static <T> T getExpressionValue(@Nullable Object root, @Nullable String expressionString, @NonNull Class<? extends T> clazz) {
        if (root == null) {
            return null;
        }
        Expression expression = getExpression(expressionString);
        if (expression == null) {
            return null;
        }

        return expression.getValue(root, clazz);
    }

    @Nullable
    public static <T> T getExpressionValue(@Nullable Object root, @Nullable String expressionString) {
        if (root == null) {
            return null;
        }
        Expression expression = getExpression(expressionString);
        if (expression == null) {
            return null;
        }
        //noinspection unchecked
        return (T) expression.getValue(root);
    }

    /**
     * 求值
     *
     * @param root              根对象
     * @param expressionStrings Spring EL表达式
     * @param <T>               泛型 这里的泛型要慎用,大多数情况下要使用Object接收避免出现转换异常
     * @return 结果集
     */
    public static <T> T[] getExpressionValue(@Nullable Object root, @Nullable String... expressionStrings) {
        if (root == null) {
            return null;
        }
        if (ArrayUtils.isEmpty(expressionStrings)) {
            return null;
        }
        //noinspection ConstantConditions
        Object[] values = new Object[expressionStrings.length];
        for (int i = 0; i < expressionStrings.length; i++) {
            //noinspection unchecked
            values[i] = (T) getExpressionValue(root, expressionStrings[i]);
        }
        //noinspection unchecked
        return (T[]) values;
    }

    /**
     * 表达式条件求值
     * 如果为值为null则返回false,
     * 如果为布尔类型直接返回,
     * 如果为数字类型则判断是否大于0
     *
     * @param root             根对象
     * @param expressionString Spring EL表达式
     * @return 值
     */
    @Nullable
    public static boolean getConditionValue(@Nullable Object root, @Nullable String expressionString) {
        Object value = getExpressionValue(root, expressionString);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue() > 0;
        }
        return true;
    }

    /**
     * 表达式条件求值
     *
     * @param root              根对象
     * @param expressionStrings Spring EL表达式数组
     * @return 值
     */
    @Nullable
    public static boolean getConditionValue(@Nullable Object root, @Nullable String... expressionStrings) {
        if (root == null) {
            return false;
        }
        if (ArrayUtils.isEmpty(expressionStrings)) {
            return false;
        }
        //noinspection ConstantConditions
        for (String expressionString : expressionStrings) {
            if (!getConditionValue(root, expressionString)) {
                return false;
            }
        }
        return true;
    }

}
