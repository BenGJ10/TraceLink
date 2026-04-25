package com.urlshortner.tracelink.service;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.urlshortner.tracelink.models.User;

import java.util.Collection;
import java.util.Collections;

@Data
@NoArgsConstructor
/*
    This class is an implementation of the UserDetails interface provided by Spring Security. 
    It serves as a bridge between the application's user model and Spring Security's authentication mechanism.
    It'll help Spring Security understand how to retrieve user information and authorities (roles) for authentication and authorization purposes.
    
    Why implement UserDetails? 
    Spring Security uses the UserDetails interface to represent user information. By implementing this interface, 
    you can customize how user data is stored and retrieved, allowing you to integrate with your application's user model and database structure.
*/
public class UserDetailsImplementation implements UserDetails {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String username;
    private String password;
    private String email;

    // Describes the authorities (roles) granted to the user
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImplementation(Long id, String username, String password, String email, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = authorities;
    }

    /* Method to build a UserDetailsImplementation instance from a User entity
       This method takes a User object and extracts the necessary information to create a UserDetailsImplementation instance,
       including the user's ID, username, email, password, and authorities (roles) */
    public static UserDetailsImplementation build(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
        return new UserDetailsImplementation(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                Collections.singletonList(authority)
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}
