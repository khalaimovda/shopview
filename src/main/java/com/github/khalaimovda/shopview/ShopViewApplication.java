package com.github.khalaimovda.shopview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ShopViewApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopViewApplication.class, args);
	}

}
