package com.example.groceryserver.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.groceryserver.service.AuthService;
import com.example.groceryserver.util.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SellerAuthenticationFilter extends OncePerRequestFilter {

	private final AuthService authService;
	private final JwtUtil jwtUtil;

	public SellerAuthenticationFilter(AuthService authService, JwtUtil jwtUtil) {
		this.authService = authService;
		this.jwtUtil = jwtUtil;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getServletPath();
		return "OPTIONS".equalsIgnoreCase(request.getMethod())
				|| !path.startsWith("/api/seller/")
				|| "/api/seller/login".equals(path);
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String token = findCookie(request, "sellerToken");
		if (token == null || !authService.isValidToken(token, "SELLER")) {
			writeUnauthorized(response, "Authentication required");
			return;
		}

		Claims claims = jwtUtil.getClaims(token);
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				claims.getSubject(), null, List.of(new SimpleGrantedAuthority("ROLE_SELLER")));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		try {
			filterChain.doFilter(request, response);
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

	private String findCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}

	private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write("{\"success\":false,\"message\":\"" + message + "\"}");
	}
}
