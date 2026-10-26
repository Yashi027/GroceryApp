package com.example.groceryserver.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CartItem {

	private String id;
	private int quantity;
}
