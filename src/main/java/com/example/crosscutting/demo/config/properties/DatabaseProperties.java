package com.example.crosscutting.demo.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "data")
@Getter @Setter
public class DatabaseProperties {
    private String filePath;
    private String defaultSchema;
    private List<String> defaultTables;

}
