package com.example.groceryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class GroceryserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(GroceryserverApplication.class, args);
	}

}
