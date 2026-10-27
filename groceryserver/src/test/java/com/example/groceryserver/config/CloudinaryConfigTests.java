package com.example.groceryserver.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.cloudinary.Cloudinary;

class CloudinaryConfigTests {

	@Test
	void trimsCloudinaryCredentialsFromConfiguration() {
		Cloudinary cloudinary = new CloudinaryConfig()
				.cloudinary(" grocery_app ", " api-key ", " api-secret ");

		assertEquals("grocery_app", cloudinary.config.cloudName);
		assertEquals("api-key", cloudinary.config.apiKey);
		assertEquals("api-secret", cloudinary.config.apiSecret);
	}
}
