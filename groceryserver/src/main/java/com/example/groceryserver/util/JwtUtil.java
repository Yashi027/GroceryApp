package com.example.groceryserver.util;

import java.time.Instant;
import java.security.SecureRandom;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
	private final SecretKey signingKey;
	private final long expirationSeconds;

	public JwtUtil(
			@Value("${app.jwt.secret:}") String base64Secret,
			@Value("${app.jwt.expiration-seconds:3600}") long expirationSeconds) {
		if (expirationSeconds <= 0) {
			throw new IllegalStateException("app.jwt.expiration-seconds must be greater than zero");
		}
		if (base64Secret == null || base64Secret.isBlank()) {
			byte[] keyBytes = new byte[32];
			new SecureRandom().nextBytes(keyBytes);
			this.signingKey = Keys.hmacShaKeyFor(keyBytes);
			log.warn("JWT_SECRET is not configured. A temporary signing key was generated; "
					+ "tokens will become invalid after the application restarts. "
					+ "Configure JWT_SECRET for persistent sessions.");
			this.expirationSeconds = expirationSeconds;
			return;
		}
		byte[] keyBytes;
		try {
			keyBytes = Decoders.BASE64.decode(base64Secret.trim());
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException(
					"JWT_SECRET must be a valid Base64-encoded key of at least 32 bytes.", exception);
		}
		if (keyBytes.length < 32) {
			throw new IllegalStateException(
					"JWT_SECRET decodes to fewer than 32 bytes. Generate a random key of at least 32 bytes and Base64-encode it.");
		}
		try {
			this.signingKey = Keys.hmacShaKeyFor(keyBytes);
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException(
					"JWT_SECRET is not a valid HMAC signing key. Set it to a Base64-encoded random key of at least 32 bytes.",
					exception);
		}
		this.expirationSeconds = expirationSeconds;
	}

	public String generateToken(String subject, String role, String userId) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(subject)
				.claim("role", role)
				.claim("userId", userId)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusSeconds(expirationSeconds)))
				.signWith(signingKey)
				.compact();
	}

	public boolean validateToken(String token, String expectedRole) {
		try {
			Claims claims = parseClaims(token);
			return expectedRole.equals(claims.get("role", String.class))
					&& claims.getSubject() != null
					&& claims.getExpiration() != null
					&& claims.getExpiration().after(new Date());
		} catch (JwtException | IllegalArgumentException exception) {
			return false;
		}
	}

	public Claims getClaims(String token) {
		return parseClaims(token);
	}

	public long getExpirationSeconds() {
		return expirationSeconds;
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
