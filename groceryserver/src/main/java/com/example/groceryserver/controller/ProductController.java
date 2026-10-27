package com.example.groceryserver.controller;

import java.util.Map;
import java.util.List;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.example.groceryserver.dto.ProductRequest;
import com.example.groceryserver.exception.ProductUploadException;
import com.example.groceryserver.service.ProductService;

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
			MultipartException.class,
			IllegalArgumentException.class
	})
	public ResponseEntity<Map<String, Object>> handleInvalidRequest(Exception exception) {
		String message = exception instanceof IllegalArgumentException
				? exception.getMessage()
				: "Product details or image files are missing or invalid";
		return ResponseEntity.badRequest().body(response(false, message));
	}

	@ExceptionHandler(ProductUploadException.class)
	public ResponseEntity<Map<String, Object>> handleProductUpload(ProductUploadException exception) {
		log.error("Product upload failed", exception);
		return ResponseEntity.status(exception.getStatus())
				.body(response(false, exception.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleUnexpectedError(Exception exception) {
		log.error("Unexpected error while adding product", exception);
		return ResponseEntity.internalServerError()
				.body(response(false, "Product could not be added"));
	}

	private Map<String, Object> response(boolean success, String message) {
		return Map.of("success", success, "message", message);
	}
}
