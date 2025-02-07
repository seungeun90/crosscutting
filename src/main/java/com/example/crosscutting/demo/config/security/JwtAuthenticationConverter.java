package com.example.crosscutting.demo.config.security;

import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

@AllArgsConstructor
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private List<String> clientIds;

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        Collection<? extends GrantedAuthority> grantedAuthorities = extractResourceRoles(source);
        return new CustomAuthenticationToken(
                extractName(source),
                extractUserId(source),
                source.getTokenValue(),
                extractTenant(source),
                grantedAuthorities);
    }
    private String extractUserId(Jwt jwt){
        return jwt.getClaim("sub");
    }

    private Tenant extractTenant(Jwt jwt){
        Map<String, Object> organizationClaims = jwt.getClaim("organization");
        if (organizationClaims == null || organizationClaims.isEmpty()) return null;

        for (Map.Entry<String, Object> entry : organizationClaims.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> organization = (Map<String, Object>) entry.getValue();

            String id = String.valueOf(organization.get("id"));
            String code = extractFirstCode((List<String>) organization.get("code"));

            return new Tenant(id, key, code);
        }
        return null;
    }
    private String extractFirstCode(List<String> codes) {
        return CollectionUtils.isEmpty(codes) ? null : codes.get(0);
    }
    private String extractName(Jwt jwt){
        return jwt.getClaim("name");
    }

    /**
     * 예를 들어 keycloak 을 oauth2-provider로 쓰면
     * jwt 내 client id를 key로 values를 추출가능
     * */
    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        var resourceAccess = new HashMap<>(jwt.getClaim("resource_access"));
        var resourceRoles = new ArrayList<>();

        clientIds.stream().forEach(id -> {
            if (resourceAccess.containsKey(id)) {
                var resource = (Map<String, List<String>>) resourceAccess.get(id);
                resource.get("roles").forEach(role -> resourceRoles.add(role));
            }
        });

        return resourceRoles.isEmpty() ? emptySet() : resourceRoles.stream().map(r -> new SimpleGrantedAuthority(String.valueOf(r))).collect(toSet());
    }
}
