package com.example.groceryserver.dto;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductResponse {

	@JsonProperty("_id")
	private String id;

	private String name;
	private BigDecimal price;
	private BigDecimal offerPrice;
	private List<String> images;
	private String category;
	private boolean inStock;
}
