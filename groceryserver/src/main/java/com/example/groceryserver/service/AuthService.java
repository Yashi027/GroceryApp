package com.example.groceryserver.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.groceryserver.dto.LoginRequest;
import com.example.groceryserver.dto.UserLoginResult;
import com.example.groceryserver.dto.UserResponse;
import com.example.groceryserver.exception.UnauthorizedException;
import com.example.groceryserver.model.User;
import com.example.groceryserver.repository.UserRepository;
import com.example.groceryserver.util.JwtUtil;

import io.jsonwebtoken.Claims;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final String sellerEmail;
	private final String encodedSellerPassword;

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtUtil jwtUtil,
			@Value("${app.seller.email}") String sellerEmail,
			@Value("${app.seller.password}") String sellerPassword) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
		this.sellerEmail = sellerEmail.trim().toLowerCase(Locale.ROOT);
		this.encodedSellerPassword = passwordEncoder.encode(sellerPassword);
	}

	public UserLoginResult loginUser(LoginRequest request) {
		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

		if (!passwordMatchesAndUpgrade(user, request.getPassword())) {
			throw new UnauthorizedException("Invalid email or password");
		}

		String token = jwtUtil.generateToken(user.getEmail(), "USER", user.getId());
		return new UserLoginResult(token, new UserResponse(user.getId(), user.getName(), user.getEmail()));
	}

	public String loginSeller(LoginRequest request) {
		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
		if (!sellerEmail.equals(email) || !passwordEncoder.matches(request.getPassword(), encodedSellerPassword)) {
			throw new UnauthorizedException("Invalid email or password");
		}
		return jwtUtil.generateToken(sellerEmail, "SELLER", null);
	}

	public boolean isValidToken(String token, String expectedRole) {
		return jwtUtil.validateToken(token, expectedRole);
	}

	public Claims getTokenClaims(String token) {
		return jwtUtil.getClaims(token);
	}

	public long getTokenExpirationSeconds() {
		return jwtUtil.getExpirationSeconds();
	}

	private boolean passwordMatchesAndUpgrade(User user, String rawPassword) {
		String storedPassword = user.getPassword();
		if (storedPassword == null) {
			return false;
		}

		if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")
				|| storedPassword.startsWith("$2y$")) {
			return passwordEncoder.matches(rawPassword, storedPassword);
		}

		boolean legacyMatch = MessageDigest.isEqual(
				rawPassword.getBytes(StandardCharsets.UTF_8),
				storedPassword.getBytes(StandardCharsets.UTF_8));
		if (legacyMatch) {
			user.setPassword(passwordEncoder.encode(rawPassword));
			userRepository.save(user);
		}
		return legacyMatch;
	}

}
