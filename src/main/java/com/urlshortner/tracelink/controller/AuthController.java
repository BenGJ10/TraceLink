package com.urlshortner.tracelink.controller;

import com.urlshortner.tracelink.dto.LoginRequest;
import com.urlshortner.tracelink.dto.RegisterRequest;
import com.urlshortner.tracelink.models.User;
import com.urlshortner.tracelink.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private UserService userService;

    // Endpoint for user login, accepting a LoginRequest object and authenticating the user using the UserService.
    @PostMapping("/public/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(userService.authenticateUser(loginRequest));
    }

    // Endpoint for user registration, accepting a RegisterRequest object and creating a new User entity based on the provided information. 
    // The new user is then registered using the UserService, and a success message is returned in the response.
    @PostMapping("/public/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest){
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        user.setEmail(registerRequest.getEmail());
        user.setRole("ROLE_USER");
        userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully");
    }
}
