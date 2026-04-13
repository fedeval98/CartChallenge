package com.fedeval.cartservicechallenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CartServiceChallengeApplication {

	public static void main(String[] args) {
		SpringApplication.run(CartServiceChallengeApplication.class, args);
	}


}
