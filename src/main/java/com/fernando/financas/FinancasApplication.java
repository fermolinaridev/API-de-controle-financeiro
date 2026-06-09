package com.fernando.financas;

import com.fernando.financas.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
@EnableScheduling
public class FinancasApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinancasApplication.class, args);
	}

}
