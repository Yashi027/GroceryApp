package com.example.groceryserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginResult {

	private String token;
	private UserResponse user;
}
