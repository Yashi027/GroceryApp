package com.example.groceryserver.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import jakarta.validation.Validation;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.example.groceryserver.dto.ProductRequest;
import com.example.groceryserver.service.ProductService;

class ProductControllerTests {

	private final ProductService productService = mock(ProductService.class);
	private final MockMvc mockMvc = MockMvcBuilders
			.standaloneSetup(new ProductController(
					productService, Validation.buildDefaultValidatorFactory().getValidator()))
			.build();

	@Test
	void acceptsJsonDetailsPartSentAsOctetStream() throws Exception {
		MockMultipartFile details = new MockMultipartFile(
				"details",
				"details.json",
				MediaType.APPLICATION_OCTET_STREAM_VALUE,
				("""
						{
						  "name": "Organic Apples",
						  "description": "Fresh organic apples",
						  "price": 5.99,
						  "offerPrice": 4.99,
						  "category": "Fruits",
						  "inStock": true
						}
						""").getBytes(StandardCharsets.UTF_8));
		MockMultipartFile image = new MockMultipartFile(
				"images", "apple.png", MediaType.IMAGE_PNG_VALUE, new byte[] { 1, 2, 3 });

		mockMvc.perform(multipart("/api/product/add")
						.file(details)
						.file(image)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Product added successfully"));

		verify(productService).addProduct(any(ProductRequest.class), anyList());
	}
}
