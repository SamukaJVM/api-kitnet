package com.kitnet.infrastructure.viacep;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kitnet.viacep")
public record ViaCepProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {
}
