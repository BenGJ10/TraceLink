package com.urlshortner.tracelink.service;

import com.urlshortner.tracelink.models.User;
import com.urlshortner.tracelink.repository.UserRepository;
import lombok.AllArgsConstructor;
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

    public User registerUser(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User findByUsername(String name) {
        return userRepository.findByUsername(name).orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + name)
        );
    }
}
