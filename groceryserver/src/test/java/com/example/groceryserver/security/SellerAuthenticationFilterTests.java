package com.example.groceryserver.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import io.jsonwebtoken.Claims;

import com.example.groceryserver.service.AuthService;
import com.example.groceryserver.util.JwtUtil;

class SellerAuthenticationFilterTests {

	private final SellerAuthenticationFilter filter = new SellerAuthenticationFilter(
			mock(AuthService.class), mock(JwtUtil.class));

	@ParameterizedTest
	@ValueSource(strings = {
			"/api/seller/is-auth/",
			"/api/product/add",
			"/api/product/stock",
			"/api/order/seller"
	})
	void rejectsProtectedEndpointsWithoutSellerToken(String path) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
		request.setServletPath(path);
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertEquals(401, response.getStatus());
	}

	@Test
	void allowsSellerLogoutWithoutSellerToken() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/seller/logout");
		request.setServletPath("/api/seller/logout");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertEquals(200, response.getStatus());
		assertEquals(request, chain.getRequest());
	}

	@Test
	void acceptsValidSellerBearerToken() throws Exception {
		AuthService authService = mock(AuthService.class);
		JwtUtil jwtUtil = mock(JwtUtil.class);
		Claims claims = mock(Claims.class);
		when(authService.isValidToken("seller-token", "SELLER")).thenReturn(true);
		when(jwtUtil.getClaims("seller-token")).thenReturn(claims);
		when(claims.getSubject()).thenReturn("seller");

		SellerAuthenticationFilter authenticatedFilter = new SellerAuthenticationFilter(authService, jwtUtil);
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/product/add");
		request.setServletPath("/api/product/add");
		request.addHeader("Authorization", "Bearer seller-token");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		authenticatedFilter.doFilter(request, response, chain);

		assertEquals(200, response.getStatus());
		assertEquals(request, chain.getRequest());
	}
}
