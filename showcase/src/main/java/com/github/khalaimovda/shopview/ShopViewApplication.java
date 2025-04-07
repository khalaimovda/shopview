package com.github.khalaimovda.shopview;

import com.github.khalaimovda.showcase.api.DefaultApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ShopViewApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(ShopViewApplication.class, args);
	}

	@Autowired
	public DefaultApi paymentServiceApi;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		System.out.println("Start");

		paymentServiceApi.getApiClient().setBasePath("http://localhost:8081/v1");

		paymentServiceApi
			.apiBalanceGet()
			.doOnNext(System.out::println)
			.block();

		System.out.println("Finish");
	}
}
