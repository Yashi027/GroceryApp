package com.example.groceryserver.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.groceryserver.dto.RegisterRequest;
import com.example.groceryserver.dto.UserResponse;
import com.example.groceryserver.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/register")
	public ResponseEntity<Map<String, Object>> register(
			@Valid @RequestBody RegisterRequest request) {
		UserResponse user = userService.register(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(Map.of(
						"success", true,
						"message", "User registered successfully",
						"user", user));
	}
}
