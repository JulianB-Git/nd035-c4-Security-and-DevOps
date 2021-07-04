package com.example.demo.controllers;

import com.example.demo.constants.LoggerMessage;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	private static Logger logger = LoggerFactory.getLogger(UserController.class);

	@GetMapping("/id/{id}")
	public ResponseEntity<User> findById(@PathVariable Long id) {
		return ResponseEntity.of(userRepository.findById(id));
	}
	
	@GetMapping("/{username}")
	public ResponseEntity<Object> findByUserName(@PathVariable String username) {
		User user = userRepository.findByUsername(username);
		return user == null ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(LoggerMessage.USER_NOT_FOUND, null)) : ResponseEntity.ok(user);
	}
	
	@PostMapping("/create")
	public ResponseEntity<Object> createUser(@RequestBody CreateUserRequest createUserRequest) {
		User user = new User();
		user.setUsername(createUserRequest.getUsername());

		if (userRepository.findByUsername(user.getUsername()) != null) {
			logger.error(LoggerMessage.SIGNUP_ERROR_INVALID_USERNAME);
			return ResponseEntity.ok(new ApiError(LoggerMessage.SIGNUP_ERROR_INVALID_USERNAME, null));
		}

		if(createUserRequest.getPassword().length()< 7 ){
			logger.error(LoggerMessage.SIGNUP_ERROR_PASSWORD_LENGTH);
			return ResponseEntity.badRequest().body(new ApiError(LoggerMessage.SIGNUP_ERROR_PASSWORD_LENGTH, null));
		}

		if(!createUserRequest.getPassword().equals(createUserRequest.getConfirmPassword())){
			logger.error(LoggerMessage.SIGNUP_ERROR_PASSWORD_MATCH);
			return ResponseEntity.badRequest().body(new ApiError(LoggerMessage.SIGNUP_ERROR_PASSWORD_MATCH, null));
		}

		Cart cart = new Cart();
		cartRepository.save(cart);
		user.setCart(cart);

		user.setPassword(bCryptPasswordEncoder.encode(createUserRequest.getPassword()));

		userRepository.save(user);
		logger.info(LoggerMessage.SIGNUP_SUCCESS + user.getUsername());
		return ResponseEntity.ok(user);
	}
	
}
