package com.example.crosscutting.demo.common.db.interceptor;

import com.example.crosscutting.demo.common.db.Schema;
import com.example.crosscutting.demo.common.db.Table;
import com.example.crosscutting.demo.common.tenant.TenantContextHolder;
import com.example.crosscutting.demo.config.properties.DatabaseProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.sql.Connection;


@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaSettingInterceptor implements Interceptor {
    private final DatabaseProperties dbProperties;

    /**
     * 쿼리 실행 시 스키마 셋팅
     * */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        /**
         * 고객사 스키마 셋팅
         * */
        String tenant = TenantContextHolder.getTenant();
        /**
         * 공통 테이블 접근이라면 default schema 셋팅
         * */
        String queryID = ((MappedStatement) invocation.getArgs()[0]).getId();
        String className = queryID.substring(0, queryID.lastIndexOf("."));
        Class<?> mapperClass = Class.forName(className);
        if (mapperClass.isAnnotationPresent(Table.class)) {
            Table tableAnnotation = mapperClass.getAnnotation(Table.class);
            if(Schema.GLOBAL.equals(tableAnnotation.schema())) {
                tenant = dbProperties.getDefaultSchema();
            }
        }

        Connection connection = ((Executor) invocation.getTarget()).getTransaction().getConnection();
        connection.createStatement().execute("SET search_path TO \"" + tenant + "\"");

        return invocation.proceed();
    }
}
