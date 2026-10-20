package com.kitnet.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class CepFinderTestConfiguration {

	@Bean
	@Primary
	StubEnderecoCepFinder stubEnderecoCepFinder() {
		return new StubEnderecoCepFinder();
	}

}
