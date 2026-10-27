package com.example.groceryserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Configuration
public class CloudinaryConfig {

	@Bean
	public Cloudinary cloudinary(
			@Value("${cloudinary.cloud-name:}") String cloudName,
			@Value("${cloudinary.api-key:}") String apiKey,
			@Value("${cloudinary.api-secret:}") String apiSecret) {
		return new Cloudinary(ObjectUtils.asMap(
				"cloud_name", cloudName.trim(),
				"api_key", apiKey.trim(),
				"api_secret", apiSecret.trim(),
				"secure", true));
	}
}
