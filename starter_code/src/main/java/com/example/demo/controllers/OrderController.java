package com.example.demo.controllers;

import java.util.List;

import com.example.demo.constants.LoggerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;

@RestController
@RequestMapping("/api/order")
public class OrderController {
	
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private OrderRepository orderRepository;

	private static Logger logger = LoggerFactory.getLogger(OrderController.class);
	
	@PostMapping("/submit/{username}")
	public ResponseEntity<Object> submit(@PathVariable String username) {
		User user = userRepository.findByUsername(username);
		if(user == null) {
			logger.error("Submitting order error: "+LoggerMessage.USER_NOT_FOUND);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(LoggerMessage.USER_NOT_FOUND, null));
		}
		if (user.getCart().getItems().size() == 0){
			logger.error("Submitting order error: "+LoggerMessage.CART_IS_EMPTY);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(LoggerMessage.CART_IS_EMPTY, null));
		}

		UserOrder order = UserOrder.createFromCart(user.getCart());
		orderRepository.save(order);
		logger.error(LoggerMessage.ORDER_SUCCESS+username);
		return ResponseEntity.ok(order);
	}
	
	@GetMapping("/history/{username}")
	public ResponseEntity<List<UserOrder>> getOrdersForUser(@PathVariable String username) {
		User user = userRepository.findByUsername(username);
		if(user == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(orderRepository.findByUser(user));
	}
}
