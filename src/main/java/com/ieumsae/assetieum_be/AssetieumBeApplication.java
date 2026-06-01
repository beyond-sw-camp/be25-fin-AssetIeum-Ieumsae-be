package com.ieumsae.assetieum_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class AssetieumBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssetieumBeApplication.class, args);
	}

}
