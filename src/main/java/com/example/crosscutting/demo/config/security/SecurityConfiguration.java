package com.example.crosscutting.demo.config.security;

import com.example.crosscutting.demo.config.properties.SecurityProperties;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@AllArgsConstructor
public class SecurityConfiguration {
    private final SecurityProperties securityProperties;

    @Bean
    SecurityFilterChain webOAuth2FilterChain(final HttpSecurity http) throws Exception {
        http.oauth2ResourceServer(oAuth2ResourceServerConfigurer ->
                oAuth2ResourceServerConfigurer.jwt(jwtConfigurer ->
                        jwtConfigurer.jwtAuthenticationConverter(
                                new JwtAuthenticationConverter(Arrays.asList("getClientId()")))));

        List<String> permitGetPath = securityProperties.getPermitGetPath();
        List<String> permitPostPath = securityProperties.getPermitPostPath();

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.GET, permitGetPath.toArray(String[]::new))
                .permitAll()
                .requestMatchers(HttpMethod.POST, permitPostPath.toArray(String[]::new)).permitAll()
                .anyRequest().authenticated()
                );
        http.cors(httpSecurityCorsConfigurer ->
                httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()));
        http.csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable());
        return http.build();

    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}