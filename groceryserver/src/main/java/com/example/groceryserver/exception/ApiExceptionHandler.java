package com.example.groceryserver.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(EmailAlreadyExistsException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(errorResponse(exception.getMessage()));
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(errorResponse(exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getDefaultMessage())
				.distinct()
				.collect(Collectors.joining(", "));
		if (message.isBlank()) {
			message = "Request validation failed";
		}
		return ResponseEntity.badRequest().body(errorResponse(message));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException exception) {
		String message = exception.getConstraintViolations().stream()
				.map(violation -> violation.getMessage())
				.distinct()
				.collect(Collectors.joining(", "));
		return ResponseEntity.badRequest().body(errorResponse(message));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleUnreadableMessage(HttpMessageNotReadableException exception) {
		return ResponseEntity.badRequest()
				.body(errorResponse("Request body is missing or invalid"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleUnexpectedException(Exception exception) {
		log.error("Unhandled exception while processing request", exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(errorResponse("An unexpected error occurred"));
	}

	private Map<String, Object> errorResponse(String message) {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("success", false);
		response.put("message", message);
		response.put("user", null);
		return response;
	}
}
