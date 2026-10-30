package com.example.groceryserver.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Validation;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.example.groceryserver.dto.ProductRequest;
import com.example.groceryserver.model.Product;
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

	@Test
	void listsProductsWithFieldsAvailableToUsersAndSellers() throws Exception {
		Product product = new Product();
		product.setId("product-123");
		product.setName("Organic Apples");
		product.setPrice(new BigDecimal("5.99"));
		product.setOfferPrice(new BigDecimal("4.99"));
		product.setImages(List.of("https://example.com/apple.png"));
		product.setCategory("Fruits");
		product.setInStock(true);
		when(productService.getProducts()).thenReturn(List.of(product));

		mockMvc.perform(get("/api/product/list").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.products[0]._id").value("product-123"))
				.andExpect(jsonPath("$.products[0].name").value("Organic Apples"))
				.andExpect(jsonPath("$.products[0].price").value(5.99))
				.andExpect(jsonPath("$.products[0].offerPrice").value(4.99))
				.andExpect(jsonPath("$.products[0].images[0]").value("https://example.com/apple.png"))
				.andExpect(jsonPath("$.products[0].category").value("Fruits"))
				.andExpect(jsonPath("$.products[0].inStock").value(true));

		verify(productService).getProducts();
	}

	@Test
	void returnsSuccessWithAnEmptyListWhenNoProductsExist() throws Exception {
		when(productService.getProducts()).thenReturn(List.of());

		mockMvc.perform(get("/api/product/list").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.products").isEmpty());
	}

	@Test
	void getsProductByIdWithSingleImageLink() throws Exception {
		Product product = new Product();
		product.setId("691a45");
		product.setName("Organic Apples");
		product.setPrice(new BigDecimal("5.99"));
		product.setOfferPrice(new BigDecimal("4.99"));
		product.setImages(List.of("https://example.com/apple.png", "https://example.com/apple-2.png"));
		product.setCategory("Fruits");
		product.setInStock(true);
		when(productService.getProductById("691a45")).thenReturn(Optional.of(product));

		mockMvc.perform(get("/api/product/id").param("id", "691a45"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.product._id").value("691a45"))
				.andExpect(jsonPath("$.product.name").value("Organic Apples"))
				.andExpect(jsonPath("$.product.price").value(5.99))
				.andExpect(jsonPath("$.product.offerPrice").value(4.99))
				.andExpect(jsonPath("$.product.image").value("https://example.com/apple.png"))
				.andExpect(jsonPath("$.product.category").value("Fruits"))
				.andExpect(jsonPath("$.product.inStock").value(true));

		verify(productService).getProductById("691a45");
	}

	@Test
	void returnsNotFoundWhenProductIdDoesNotExist() throws Exception {
		when(productService.getProductById("missing")).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/product/id").param("id", "missing"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.product").value(org.hamcrest.Matchers.nullValue()))
				.andExpect(jsonPath("$.message").value("Product not found"));
	}

	@Test
	void returnsBadRequestWhenProductIdIsMissing() throws Exception {
		mockMvc.perform(get("/api/product/id"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.product").value(org.hamcrest.Matchers.nullValue()));
	}

	@Test
	void returnsServerErrorWhenProductLookupFails() throws Exception {
		when(productService.getProductById("691a45")).thenThrow(new RuntimeException("Database unavailable"));

		mockMvc.perform(get("/api/product/id").param("id", "691a45"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void getsProductByIdFromJsonPostBody() throws Exception {
		Product product = new Product();
		product.setId("6ae0c547321616f23e65294d");
		product.setName("Organic Apples");
		product.setPrice(new BigDecimal("5.99"));
		product.setOfferPrice(new BigDecimal("4.99"));
		product.setImages(List.of("https://example.com/apple.png"));
		product.setCategory("Fruits");
		product.setInStock(true);
		when(productService.getProductById("6ae0c547321616f23e65294d")).thenReturn(Optional.of(product));

		mockMvc.perform(post("/api/product/id")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"id\":\"6ae0c547321616f23e65294d\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.product._id").value("6ae0c547321616f23e65294d"))
				.andExpect(jsonPath("$.product.name").value("Organic Apples"))
				.andExpect(jsonPath("$.product.price").value(5.99))
				.andExpect(jsonPath("$.product.offerPrice").value(4.99))
				.andExpect(jsonPath("$.product.image").value("https://example.com/apple.png"))
				.andExpect(jsonPath("$.product.category").value("Fruits"))
				.andExpect(jsonPath("$.product.inStock").value(true));

		verify(productService).getProductById("6ae0c547321616f23e65294d");
	}

	@Test
	void returnsBadRequestWhenPostBodyHasBlankProductId() throws Exception {
		mockMvc.perform(post("/api/product/id")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"id\":\" \"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.product").value(org.hamcrest.Matchers.nullValue()));
	}

	@Test
	void returnsProductFailureStructureWhenPostLookupFails() throws Exception {
		when(productService.getProductById("691a45")).thenThrow(new RuntimeException("Database unavailable"));

		mockMvc.perform(post("/api/product/id")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"id\":\"691a45\"}"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.product").value(org.hamcrest.Matchers.nullValue()));
	}
}
