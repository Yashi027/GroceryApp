package com.example.groceryserver.exception;

import org.springframework.http.HttpStatus;

public class ProductUploadException extends RuntimeException {

	private final HttpStatus status;

	public ProductUploadException(String message, HttpStatus status) {
		super(message);
		this.status = status;
	}

	public ProductUploadException(String message, HttpStatus status, Throwable cause) {
		super(message, cause);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
