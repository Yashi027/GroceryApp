package com.example.groceryserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.groceryserver.dto.LoginRequest;
import com.example.groceryserver.dto.UserLoginResult;
import com.example.groceryserver.exception.UnauthorizedException;
import com.example.groceryserver.model.User;
import com.example.groceryserver.repository.UserRepository;
import com.example.groceryserver.service.AuthService;
import com.example.groceryserver.util.JwtUtil;

class AuthServiceTests {

	private static final String TEST_JWT_SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

	@Test
	void missingJwtSecretCreatesTemporaryUsableSigningKey() {
		JwtUtil jwtUtil = new JwtUtil("", 3600);
		String token = jwtUtil.generateToken("test@example.com", "USER", "user-123");

		assertTrue(jwtUtil.validateToken(token, "USER"));
	}

	@Test
	void shortJwtSecretFailsWithActionableMessage() {
		IllegalStateException exception = assertThrows(
				IllegalStateException.class,
				() -> new JwtUtil("c2hvcnQ=", 3600));

		assertTrue(exception.getMessage().contains("fewer than 32 bytes"));
	}

	@Test
	void userLoginValidatesBcryptPasswordAndCreatesUserToken() {
		UserRepository repository = mock(UserRepository.class);
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		JwtUtil jwtUtil = new JwtUtil(TEST_JWT_SECRET, 3600);
		User user = new User();
		user.setId("user-123");
		user.setName("Test User");
		user.setEmail("test@example.com");
		user.setPassword(passwordEncoder.encode("password123"));
		when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

		AuthService authService = new AuthService(
				repository, passwordEncoder, jwtUtil, "admin@groceryapp.com", "admin@123");
		UserLoginResult result = authService.loginUser(loginRequest("test@example.com", "password123"));

		assertEquals("Test User", result.getUser().getName());
		assertEquals("test@example.com", result.getUser().getEmail());
		assertEquals("user-123", result.getUser().getId());
		assertTrue(authService.isValidToken(result.getToken(), "USER"));
		assertTrue(!authService.isValidToken(result.getToken(), "SELLER"));
	}

	@Test
	void invalidUserPasswordIsRejected() {
		UserRepository repository = mock(UserRepository.class);
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		User user = new User();
		user.setEmail("test@example.com");
		user.setPassword(passwordEncoder.encode("correct-password"));
		when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
		AuthService authService = new AuthService(
				repository, passwordEncoder, new JwtUtil(TEST_JWT_SECRET, 3600),
				"admin@groceryapp.com", "admin@123");

		assertThrows(UnauthorizedException.class,
				() -> authService.loginUser(loginRequest("test@example.com", "wrong-password")));
	}

	@Test
	void successfulLoginUpgradesLegacyPlaintextPassword() {
		UserRepository repository = mock(UserRepository.class);
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		User user = new User();
		user.setId("user-123");
		user.setEmail("test@example.com");
		user.setPassword("legacy-password");
		when(repository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
		AuthService authService = new AuthService(
				repository, passwordEncoder, new JwtUtil(TEST_JWT_SECRET, 3600),
				"admin@groceryapp.com", "admin@123");

		authService.loginUser(loginRequest("test@example.com", "legacy-password"));

		assertTrue(user.getPassword().startsWith("$2"));
		assertTrue(passwordEncoder.matches("legacy-password", user.getPassword()));
		verify(repository).save(user);
	}

	@Test
	void sellerLoginCreatesSellerToken() {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		AuthService authService = new AuthService(
				mock(UserRepository.class), passwordEncoder,
				new JwtUtil(TEST_JWT_SECRET, 3600), "admin@groceryapp.com", "admin@123");

		String token = authService.loginSeller(loginRequest("admin@groceryapp.com", "admin@123"));

		assertTrue(authService.isValidToken(token, "SELLER"));
		assertTrue(!authService.isValidToken(token, "USER"));
	}

	@Test
	void invalidSellerCredentialsAreRejected() {
		AuthService authService = new AuthService(
				mock(UserRepository.class), new BCryptPasswordEncoder(),
				new JwtUtil(TEST_JWT_SECRET, 3600), "admin@groceryapp.com", "admin@123");

		assertThrows(UnauthorizedException.class,
				() -> authService.loginSeller(loginRequest("admin@groceryapp.com", "wrong-password")));
	}

	private LoginRequest loginRequest(String email, String password) {
		LoginRequest request = new LoginRequest();
		request.setEmail(email);
		request.setPassword(password);
		return request;
	}
}
