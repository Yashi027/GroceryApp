package com.example.groceryserver.model;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "products")
public class Product {

	@Id
	private String id;
	private String name;
	private String description;
	private BigDecimal price;
	private BigDecimal offerPrice;
	private String category;
	private boolean inStock;
	private List<String> images;
}
