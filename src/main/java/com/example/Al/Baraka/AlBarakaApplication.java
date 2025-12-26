package com.example.Al.Baraka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AlBarakaApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlBarakaApplication.class, args);
	}

}
