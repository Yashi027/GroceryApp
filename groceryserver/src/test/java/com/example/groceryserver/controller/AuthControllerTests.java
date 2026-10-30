package com.example.groceryserver.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.groceryserver.service.AuthService;
import com.example.groceryserver.util.CookieUtil;

class AuthControllerTests {

	private final MockMvc mockMvc = MockMvcBuilders
			.standaloneSetup(new AuthController(mock(AuthService.class), new CookieUtil()))
			.build();

	@Test
	void logsOutSellerAndExpiresSellerTokenCookie() throws Exception {
		mockMvc.perform(post("/api/seller/logout"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Logged out"))
				.andExpect(header().string("Set-Cookie", containsString("sellerToken=")))
				.andExpect(header().string("Set-Cookie", containsString("Max-Age=0")))
				.andExpect(header().string("Set-Cookie", containsString("Path=/")));
	}

	@Test
	void logsOutUserAndExpiresUserTokenCookie() throws Exception {
		mockMvc.perform(post("/api/users/logout"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Logged out"))
				.andExpect(header().string("Set-Cookie", containsString("userToken=")))
				.andExpect(header().string("Set-Cookie", containsString("Max-Age=0")))
				.andExpect(header().string("Set-Cookie", containsString("Path=/")));
	}
}
