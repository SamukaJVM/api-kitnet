package com.kitnet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.kitnet.config.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ApiKitnetApplicationTests {

	@Test
	void contextLoads() {
	}

}
