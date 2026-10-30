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
		writeTokenCookie(response, request, cookieName, token, Duration.ofSeconds(maxAgeSeconds));
	}

	public void clearTokenCookie(
			HttpServletResponse response,
			HttpServletRequest request,
			String cookieName) {
		writeTokenCookie(response, request, cookieName, "", Duration.ZERO);
	}

	private void writeTokenCookie(
			HttpServletResponse response,
			HttpServletRequest request,
			String cookieName,
			String token,
			Duration maxAge) {
		ResponseCookie cookie = ResponseCookie.from(cookieName, token)
				.httpOnly(true)
				.secure(request.isSecure())
				.path("/")
				.sameSite("Lax")
				.maxAge(maxAge)
				.build();
		response.addHeader("Set-Cookie", cookie.toString());
	}
}
