package com.example.crosscutting.demo.common.tenant.aop;

import com.example.crosscutting.demo.common.tenant.TenantContextHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;


@Aspect
@Component
@Slf4j
public class TenantAspect {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String EXTRACT_FILED_TARGET = "tenant";

    @Around("@annotation(com.clp.admin.common.tenant.SetTenant) || @within(com.clp.admin.common.tenant.SetTenant)")
    public Object setTenant(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Aspect SetTenant========================");
        // 이미 TenantContextHolder에 값이 설정되어 있는지 확인
        String currentTenant = TenantContextHolder.getTenant();
        if (currentTenant != null) {
            log.info("Tenant already set: {}", currentTenant);
            return joinPoint.proceed();
        }

        for (Object arg : joinPoint.getArgs()) {
            String clientCode = extractTenantCode(arg);
            if (clientCode != null) {
                TenantContextHolder.setTenant(clientCode);
                break;
            }
        }

        if(TenantContextHolder.getTenant()==null) {
            throw new RuntimeException("Tenant 정보가 누락되었습니다.");
        }

        try {
            return joinPoint.proceed();
        } finally {
            // 테넌트 설정을 메서드 체인 끝에서만 초기화
            TenantContextHolder.clear();
            log.info("Tenant cleared====================");
        }
    }

    private String extractTenantCode(Object parameterObject) {
        if (parameterObject == null) {
            return null;
        }

        if (parameterObject instanceof String) {
            return extractFromString((String) parameterObject);
        }
        else if (parameterObject instanceof Map) {
            return (String) ((Map<?, ?>) parameterObject).getOrDefault(EXTRACT_FILED_TARGET, null);
        }
        else {
            return extractFromField(parameterObject);
        }
    }

    private String extractFromField(Object object) {
        try {
            Field field = object.getClass().getDeclaredField(EXTRACT_FILED_TARGET);
            field.setAccessible(true);
            return (String) field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.warn("Unable to access field '{}': {}", EXTRACT_FILED_TARGET, e.getMessage());
        }
        return null;
    }

    private String extractFromString(String parameterString) {
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(parameterString);
            if (jsonNode.has("clientCode")) {
                return jsonNode.get("clientCode").asText();
            }
        } catch (Exception e) {
            log.debug("Parameter is not JSON, treating as plain String: {}", parameterString);
            return null;
        }
        return null;
    }
}
