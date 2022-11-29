package com.gitegg.platform.mybatis.util;

import java.util.regex.Pattern;

/**
 * 判断参数是否有sql关键字
 * @author GitEgg
 */
public class SqlInjectionRuleUtils {
    
    /**
     * sql关键字表达式
     */
    private static String badStrReg = "\\b(and|or)\\b.{1,6}?(=|>|<|\\bin\\b|\\blike\\b)|\\/\\*.+?\\*\\/|<\\s*script\\b|\\bEXEC\\b|UNION.+?SELECT|UPDATE.+?SET|INSERT\\s+INTO.+?VALUES|(SELECT|DELETE).+?FROM|(CREATE|ALTER|DROP|TRUNCATE)\\s+(TABLE|DATABASE)";
    
    /**
     * 整体都忽略大小写
     */
    private static Pattern sqlPattern = Pattern.compile(badStrReg, Pattern.CASE_INSENSITIVE);
    
    /**
     * 解析参数SQL关键字
     * @param value
     * @return
     */
    public static Boolean hasSqlKeyWords(String value) {
        //这里需要将参数转换为小写来处理
        //不改变原值
        if (sqlPattern.matcher(value.toLowerCase()).find()) {
            return true;
        }
        return false;
    }
    
}
