package com.microfocus.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "com.microfocus")
public class SpringBootWithMetricsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWithMetricsApplication.class, args);
	}

}
