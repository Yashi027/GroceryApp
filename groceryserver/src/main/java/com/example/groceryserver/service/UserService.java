package com.example.groceryserver.service;

import java.util.Locale;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.groceryserver.dto.RegisterRequest;
import com.example.groceryserver.dto.UserResponse;
import com.example.groceryserver.exception.EmailAlreadyExistsException;
import com.example.groceryserver.model.User;
import com.example.groceryserver.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public UserResponse register(RegisterRequest request) {
		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
		if (userRepository.existsByEmail(email)) {
			throw new EmailAlreadyExistsException();
		}

		User user = new User();
		user.setName(request.getName().trim());
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.getPassword()));

		try {
			User savedUser = userRepository.save(user);
			return new UserResponse(savedUser.getId(), savedUser.getName(), savedUser.getEmail());
		} catch (DuplicateKeyException exception) {
			throw new EmailAlreadyExistsException();
		}
	}
}
