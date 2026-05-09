package com.urlshortner.tracelink.controller;

import com.urlshortner.tracelink.dto.LoginRequest;
import com.urlshortner.tracelink.dto.RegisterRequest;
import com.urlshortner.tracelink.models.User;
import com.urlshortner.tracelink.service.EmailService;
import com.urlshortner.tracelink.service.UserService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;

    // Endpoint for user login, accepting a LoginRequest object and authenticating
    // the user using the UserService.
    @PostMapping("/public/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.authenticateUser(loginRequest));
    }

    // Endpoint for user registration, accepting a RegisterRequest object and
    // creating a new User entity based on the provided information.
    // The new user is then registered using the UserService, and a success message
    // is returned in the response.
    @PostMapping("/public/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        user.setEmail(registerRequest.getEmail());
        user.setRole("ROLE_USER");
        userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/public/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        try {
            String token = userService.generatePasswordResetToken(email);
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(email, resetUrl);
            return ResponseEntity.ok(Map.of("message", "Password reset email sent"));
        } catch (IllegalArgumentException e) {
            // Return OK even if email not found to prevent email enumeration
            return ResponseEntity
                    .ok(Map.of("message", "If an account exists with this email, a reset link has been sent."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to send reset email");
        }
    }

    @PostMapping("/public/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Token and new password are required");
        }

        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/public/contact")
    public ResponseEntity<?> submitContactMessage(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String message = request.get("message");

        if (name == null || name.trim().isEmpty() || 
            email == null || email.trim().isEmpty() || 
            message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Name, email, and message are required");
        }

        try {
            emailService.sendContactMessage(name, email, message);
            return ResponseEntity.ok(Map.of("message", "Message sent successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to send message. Please try again later.");
        }
    }
}
