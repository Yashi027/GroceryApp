package com.example.groceryserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.mongodb.uri=mongodb://localhost:27017/groceryserver_test",
		"spring.mongodb.database=groceryserver_test",
		"spring.data.mongodb.auto-index-creation=false",
		"app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class GroceryserverApplicationTests {

	@Test
	void contextLoads() {
	}

}
