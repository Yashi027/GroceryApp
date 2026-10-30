package com.example.groceryserver.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductDetailResponse {

	@JsonProperty("_id")
	private String id;

	private String name;
	private BigDecimal price;
	private BigDecimal offerPrice;
	private String image;
	private String category;
	private boolean inStock;
}
