package com.example.crosscutting.demo.common.tenant.interceptor;

import com.example.crosscutting.demo.common.tenant.TenantContextHolder;
import com.example.crosscutting.demo.config.properties.SecurityProperties;
import com.example.crosscutting.demo.config.security.CustomAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantInterceptor implements HandlerInterceptor {
    private final SecurityProperties securityProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        List<String> permitGetPath = securityProperties.getPermitGetPath();
        List<String> permitPostPath = securityProperties.getPermitPostPath();
        permitGetPath.addAll(permitPostPath);
        //모든 고객이 접근 가능한 URI 예외
        String requestURI = request.getRequestURI();
        if( permitGetPath.stream().anyMatch(requestURI::contains)) {
            return true;
        }
        try {
            CustomAuthenticationToken authentication = (CustomAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            if(!authentication.isSystemAdmin()){
                if(authentication.getTenant() == null ||
                        !StringUtils.hasText(authentication.getTenant().code())){
                    throw new RuntimeException("고객 식별에 실패하였습니다.");
                }
                String code = authentication.getTenant().code();
                TenantContextHolder.setTenant(code);
            }
        } catch (Exception e) {
            log.error("auth {}" ,e);
            throw e;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContextHolder.clear();
    }

}
