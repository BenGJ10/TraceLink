package com.urlshortner.tracelink.service;

import com.urlshortner.tracelink.dto.LoginRequest;
import com.urlshortner.tracelink.models.User;
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

@Service
@AllArgsConstructor
/*
    Service class for handling user-related operations, such as registration and retrieval by username. 
*/
public class UserService {

    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
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
}
