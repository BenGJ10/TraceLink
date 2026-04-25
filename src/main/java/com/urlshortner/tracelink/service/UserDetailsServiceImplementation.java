package com.urlshortner.tracelink.service;

import com.urlshortner.tracelink.models.User;
import com.urlshortner.tracelink.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImplementation implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Transactional
    /*
        This method is called by Spring Security to load user details during authentication. It retrieves the user from the database using the provided username,
        and if found, it builds a UserDetails object that Spring Security can use for authentication and authorization. If the user is not found, it throws a UsernameNotFoundException.
    */
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unable to find user with username: " + username));
        
            return UserDetailsImplementation.build(user);
    }
}
