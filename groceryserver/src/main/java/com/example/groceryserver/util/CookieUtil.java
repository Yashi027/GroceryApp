package com.example.groceryserver.util;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtil {

	public void addTokenCookie(
			HttpServletResponse response,
			HttpServletRequest request,
			String cookieName,
			String token,
			long maxAgeSeconds) {
		ResponseCookie cookie = ResponseCookie.from(cookieName, token)
				.httpOnly(true)
				.secure(request.isSecure())
				.path("/")
				.sameSite("Lax")
				.maxAge(Duration.ofSeconds(maxAgeSeconds))
				.build();
		response.addHeader("Set-Cookie", cookie.toString());
	}
}
