package com.example.groceryserver.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductRequest {

	@NotBlank(message = "Product name is required")
	@Size(max = 200, message = "Product name must be at most 200 characters")
	private String name;

	@NotBlank(message = "Product description is required")
	private String description;

	@NotNull(message = "Product price is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Product price must be greater than zero")
	private BigDecimal price;

	@NotNull(message = "Offer price is required")
	@DecimalMin(value = "0.0", message = "Offer price must not be negative")
	private BigDecimal offerPrice;

	@NotBlank(message = "Product category is required")
	private String category;

	@NotNull(message = "Product inStock value is required")
	private Boolean inStock;
}
