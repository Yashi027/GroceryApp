package com.example.groceryserver.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class User {

	@Id
	private String id;

	private String name;

	@Indexed(unique = true)
	private String email;

	private String password;

	private Map<String, CartItem> cartItems;

	@CreatedDate
	private Instant createdAt;

	@LastModifiedDate
	private Instant updatedAt;
}
