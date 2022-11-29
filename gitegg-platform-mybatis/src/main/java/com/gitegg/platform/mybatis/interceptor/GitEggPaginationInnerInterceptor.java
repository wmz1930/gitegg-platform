package com.gitegg.platform.mybatis.interceptor;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.gitegg.platform.mybatis.util.SqlInjectionRuleUtils;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;
import org.apache.ibatis.exceptions.PersistenceException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 扩展分页插件，处理传入参数分页插件 sql注入风险和字段转驼峰
 * @author GitEgg
 */
@Data
@NoArgsConstructor
public class GitEggPaginationInnerInterceptor extends PaginationInnerInterceptor {
    
    public GitEggPaginationInnerInterceptor(DbType dbType) {
        super.setDbType(dbType);
    }
    
    /**
     * 原插件捕获异常未抛出，这里抛出异常
     * 查询SQL拼接Order By
     *
     * @param originalSql 需要拼接的SQL
     * @return ignore
     */
    @Override
    public String concatOrderBy(String originalSql, List<OrderItem> orderList) {
        try {
            Select select = (Select) CCJSqlParserUtil.parse(originalSql);
            SelectBody selectBody = select.getSelectBody();
            if (selectBody instanceof PlainSelect) {
                PlainSelect plainSelect = (PlainSelect) selectBody;
                List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
                List<OrderByElement> orderByElementsReturn = addOrderByElements(orderList, orderByElements);
                plainSelect.setOrderByElements(orderByElementsReturn);
                return select.toString();
            } else if (selectBody instanceof SetOperationList) {
                SetOperationList setOperationList = (SetOperationList) selectBody;
                List<OrderByElement> orderByElements = setOperationList.getOrderByElements();
                List<OrderByElement> orderByElementsReturn = addOrderByElements(orderList, orderByElements);
                setOperationList.setOrderByElements(orderByElementsReturn);
                return select.toString();
            } else if (selectBody instanceof WithItem) {
                // todo: don't known how to resole
                return originalSql;
            } else {
                return originalSql;
            }
        } catch (JSQLParserException e) {
            logger.warn("failed to concat orderBy from IPage, exception:\n" + e.getCause());
        }
        return originalSql;
    }
    
    @Override
    protected List<OrderByElement> addOrderByElements(List<OrderItem> orderList, List<OrderByElement> orderByElements) {
        List<OrderByElement> additionalOrderBy = orderList.stream()
                .filter(item -> StringUtils.isNotBlank(item.getColumn()))
                .map(item -> {
                    // 如果sql中存在sql关键字，有sql注入风险，所以这里直接抛出异常
                    if(SqlInjectionRuleUtils.hasSqlKeyWords(item.getColumn()))
                    {
                        throw new PersistenceException("排序字段中包含SQL关键字");
                    }
                    OrderByElement element = new OrderByElement();
                    // 因为前端传过来的有可能是字段形式，所以这里驼峰转下划线
                    element.setExpression(new Column(StringUtils.camelToUnderline(item.getColumn())));
                    element.setAsc(item.isAsc());
                    element.setAscDescPresent(true);
                    return element;
                }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orderByElements)) {
            return additionalOrderBy;
        }
        // github pull/3550 优化排序，比如：默认 order by id 前端传了name排序，设置为 order by name,id
        additionalOrderBy.addAll(orderByElements);
        return additionalOrderBy;
    }
}
