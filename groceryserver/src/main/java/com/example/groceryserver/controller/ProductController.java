package com.example.groceryserver.controller;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import com.example.groceryserver.dto.ProductDetailResponse;
import com.example.groceryserver.dto.ProductIdRequest;
import com.example.groceryserver.dto.ProductRequest;
import com.example.groceryserver.dto.ProductResponse;
import com.example.groceryserver.exception.ProductUploadException;
import com.example.groceryserver.model.Product;
import com.example.groceryserver.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestController
public class ProductController {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final ProductService productService;
	private final Validator validator;

	public ProductController(ProductService productService, Validator validator) {
		this.productService = productService;
		this.validator = validator;
	}

	@PostMapping(
			value = "/api/product/add",
			consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> addProduct(
			@RequestPart("details") String detailsJson,
			@RequestPart("images") List<MultipartFile> images) {
		ProductRequest details = parseAndValidateDetails(detailsJson);
		productService.addProduct(details, images);
		return ResponseEntity.status(HttpStatus.CREATED).body(response(true, "Product added successfully"));
	}

	@GetMapping(value = "/api/product/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> listProducts() {
		List<ProductResponse> products = productService.getProducts().stream()
				.map(this::toProductResponse)
				.toList();
		return ResponseEntity.ok(Map.of("success", true, "products", products));
	}

	@GetMapping(value = "/api/product/id", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getProductById(@RequestParam("id") String id) {
		if (id.isBlank()) {
			return ResponseEntity.badRequest().body(productLookupFailure("Product id must not be blank"));
		}

		return findProductById(id);
	}

	@PostMapping(value = "/api/product/id", consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Object>> getProductById(@RequestBody ProductIdRequest request) {
		if (request == null || request.getId() == null || request.getId().isBlank()) {
			return ResponseEntity.badRequest().body(productLookupFailure("Product id must not be blank"));
		}

		return findProductById(request.getId());
	}

	private ResponseEntity<Map<String, Object>> findProductById(String id) {
		Optional<Product> product = productService.getProductById(id.trim());
		if (product.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(productLookupFailure("Product not found"));
		}

		Product found = product.get();
		String image = found.getImages() == null || found.getImages().isEmpty()
				? null
				: found.getImages().get(0);
		ProductDetailResponse productResponse = new ProductDetailResponse(
				found.getId(),
				found.getName(),
				found.getPrice(),
				found.getOfferPrice(),
				image,
				found.getCategory(),
				found.isInStock());
		return ResponseEntity.ok(Map.of("success", true, "product", productResponse));
	}

	private ProductResponse toProductResponse(Product product) {
		return new ProductResponse(
				product.getId(),
				product.getName(),
				product.getPrice(),
				product.getOfferPrice(),
				product.getImages(),
				product.getCategory(),
				product.isInStock());
	}

	private ProductRequest parseAndValidateDetails(String detailsJson) {
		ProductRequest details;
		try {
			details = OBJECT_MAPPER.readValue(detailsJson, ProductRequest.class);
		} catch (JsonProcessingException exception) {
			throw new IllegalArgumentException("Product details must contain valid JSON", exception);
		}
		if (details == null) {
			throw new IllegalArgumentException("Product details must be a JSON object");
		}

		String validationMessage = validator.validate(details).stream()
				.map(ConstraintViolation::getMessage)
				.distinct()
				.collect(Collectors.joining(", "));
		if (!validationMessage.isBlank()) {
			throw new IllegalArgumentException(validationMessage);
		}
		return details;
	}

	@ExceptionHandler({
			HttpMessageNotReadableException.class,
			MissingServletRequestPartException.class,
			MissingServletRequestParameterException.class,
			MultipartException.class,
			IllegalArgumentException.class
	})
	public ResponseEntity<Map<String, Object>> handleInvalidRequest(
			Exception exception, HttpServletRequest request) {
		if (isProductLookupRequest(request)) {
			return ResponseEntity.badRequest().body(productLookupFailure("Product id is missing or invalid"));
		}

		String message;
		if (exception instanceof IllegalArgumentException illegalArgument) {
			message = illegalArgument.getMessage();
		} else if (exception instanceof MissingServletRequestParameterException missingParameter) {
			message = "Required request parameter is missing: " + missingParameter.getParameterName();
			if ("id".equals(missingParameter.getParameterName())) {
				return ResponseEntity.badRequest().body(productLookupFailure(message));
			}
		} else {
			message = "Product details or image files are missing or invalid";
		}
		return ResponseEntity.badRequest().body(response(false, message));
	}

	@ExceptionHandler(ProductUploadException.class)
	public ResponseEntity<Map<String, Object>> handleProductUpload(ProductUploadException exception) {
		log.error("Product upload failed", exception);
		return ResponseEntity.status(exception.getStatus())
				.body(response(false, exception.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleUnexpectedError(
			Exception exception, HttpServletRequest request) {
		log.error("Unexpected error while handling product request", exception);
		if (isProductLookupRequest(request)) {
			return ResponseEntity.internalServerError()
					.body(productLookupFailure("Product could not be retrieved"));
		}
		return ResponseEntity.internalServerError()
				.body(response(false, "Product request could not be completed"));
	}

	private Map<String, Object> response(boolean success, String message) {
		return Map.of("success", success, "message", message);
	}

	private Map<String, Object> productLookupFailure(String message) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("product", null);
		body.put("success", false);
		body.put("message", message);
		return body;
	}

	private boolean isProductLookupRequest(HttpServletRequest request) {
		return "/api/product/id".equals(request.getRequestURI());
	}
}
