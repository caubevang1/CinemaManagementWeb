package com.cinemaweb.API.Cinema.Web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiCinemaWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiCinemaWebApplication.class, args);
	}

}
