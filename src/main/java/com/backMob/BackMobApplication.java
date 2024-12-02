package com.backMob;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication

@EnableScheduling
public class BackMobApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackMobApplication.class, args);
	}

}
