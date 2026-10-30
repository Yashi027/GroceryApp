package com.example.groceryserver.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.example.groceryserver.service.AuthService;
import com.example.groceryserver.util.JwtUtil;

class UserAuthenticationFilterTests {

	private final UserAuthenticationFilter filter = new UserAuthenticationFilter(
			mock(AuthService.class), mock(JwtUtil.class));

	@Test
	void allowsUserLogoutWithoutUserToken() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/logout");
		request.setServletPath("/api/users/logout");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain chain = new MockFilterChain();

		filter.doFilter(request, response, chain);

		assertEquals(200, response.getStatus());
		assertEquals(request, chain.getRequest());
	}
}
