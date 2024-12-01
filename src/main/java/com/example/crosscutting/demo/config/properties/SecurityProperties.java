package com.example.crosscutting.demo.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "spring.security")
@Getter @Setter
public class SecurityProperties {
    private List<String> permitGetPath;
    private List<String> permitPostPath;
}
