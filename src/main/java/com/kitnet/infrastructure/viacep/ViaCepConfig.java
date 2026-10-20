package com.kitnet.infrastructure.viacep;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ViaCepProperties.class)
public class ViaCepConfig {

	@Bean
	RestClient viaCepRestClient(RestClient.Builder builder, ViaCepProperties properties) {
		HttpClientSettings settings = HttpClientSettings.defaults()
				.withConnectTimeout(properties.connectTimeout())
				.withReadTimeout(properties.readTimeout());

		ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect().build(settings);

		return builder
				.baseUrl(properties.baseUrl())
				.requestFactory(requestFactory)
				.build();
	}

}
