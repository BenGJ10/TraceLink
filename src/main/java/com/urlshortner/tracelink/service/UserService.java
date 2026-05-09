package com.urlshortner.tracelink.service;

import com.urlshortner.tracelink.dto.LoginRequest;
import com.urlshortner.tracelink.models.PasswordResetToken;
import com.urlshortner.tracelink.models.User;
import com.urlshortner.tracelink.repository.PasswordResetTokenRepository;
import com.urlshortner.tracelink.repository.UserRepository;
import com.urlshortner.tracelink.security.jwt.JWTAuthenticationResponse;
import com.urlshortner.tracelink.security.jwt.JWTUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
/*
    Service class for handling user-related operations, such as registration and retrieval by username. 
*/
public class UserService {

    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private PasswordResetTokenRepository tokenRepository;
    private AuthenticationManager authenticationManager;
    private JWTUtils jwtUtils;

    // Method to register a new user, encoding the password before saving it to the database.
    public User registerUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /*
        Method to authenticate a user based on the provided login request. It uses the AuthenticationManager to
        authenticate the user and generates a JWT token upon successful authentication, which is returned in the response.
    */
    public JWTAuthenticationResponse authenticateUser(LoginRequest loginRequest){
        // Authenticate the user using the provided username and password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                        loginRequest.getPassword()));
        
        // Set the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);        
        // Retrieve the authenticated user's details
        UserDetailsImplementation userDetails = (UserDetailsImplementation) authentication.getPrincipal();
        // Generate a JWT token for the authenticated user
        String jwt = jwtUtils.generateToken(userDetails);
        return new JWTAuthenticationResponse(jwt);
    }

    // Method to find a user by their username, throwing an exception if the user is not found.
    public User findByUsername(String name) {
        return userRepository.findByUsername(name).orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + name)
        );
    }

    /*
        Method to update the username of an existing user. It checks if the new username is already taken.
        If it is taken, it throws an IllegalArgumentException. Otherwise, it updates the user's username
        and saves it to the database.
    */
    public User updateUsername(User user, String newUsername) {
        if (userRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        user.setUsername(newUsername);
        return userRepository.save(user);
    }

    /*
        Method to update the user's password. It verifies that the current password matches the one in the database.
        If it does not match, an exception is thrown. If it matches, the new password is encoded and saved.
    */
    public void updatePassword(User user, String currentPassword, String newPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /*
        Method to securely delete a user's account from the database.
    */
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public String generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No user found with this email"));
                
        // Delete any existing tokens for this user
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        tokenRepository.save(resetToken);

        return token;
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete token after successful reset
        tokenRepository.delete(resetToken);
    }
}
