package com.example.groceryserver.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.groceryserver.dto.LoginRequest;
import com.example.groceryserver.dto.UserLoginResult;
import com.example.groceryserver.service.AuthService;
import com.example.groceryserver.util.CookieUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
public class AuthController {

	private final AuthService authService;
	private final CookieUtil cookieUtil;

	public AuthController(AuthService authService, CookieUtil cookieUtil) {
		this.authService = authService;
		this.cookieUtil = cookieUtil;
	}

	@PostMapping("/api/users/login")
	public ResponseEntity<Map<String, Object>> loginUser(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		UserLoginResult result = authService.loginUser(request);
		cookieUtil.addTokenCookie(servletResponse, servletRequest, "userToken",
				result.getToken(), authService.getTokenExpirationSeconds());
		return ResponseEntity.ok(Map.of(
				"success", true,
				"user", result.getUser()));
	}

	@PostMapping("/api/seller/login")
	public ResponseEntity<Map<String, Object>> loginSeller(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		String token = authService.loginSeller(request);
		cookieUtil.addTokenCookie(servletResponse, servletRequest, "sellerToken",
				token, authService.getTokenExpirationSeconds());
		return ResponseEntity.ok(Map.of(
				"success", true,
				"message", "Seller login successful"));
	}
}
