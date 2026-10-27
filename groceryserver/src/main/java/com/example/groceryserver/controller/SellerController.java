package com.example.groceryserver.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SellerController {

	@GetMapping("/api/seller/is-auth")
	public ResponseEntity<Map<String, Object>> isAuthenticated() {
		return ResponseEntity.ok(Map.of(
				"success", true,
				"message", "Seller is authenticated"));
	}
}
