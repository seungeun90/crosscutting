package com.example.crosscutting.demo.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aes")
@Getter @Setter
public class AESProperties {
    private String secretKey;
    private String iv;
}
