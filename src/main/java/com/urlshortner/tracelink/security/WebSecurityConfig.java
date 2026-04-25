package com.urlshortner.tracelink.security;

import com.urlshortner.tracelink.security.jwt.JWTAuthenticationFilter;
import com.urlshortner.tracelink.service.UserDetailsServiceImplementation;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
/*
    This class configures Spring Security for the application. It defines beans for JWT authentication, password encoding, and authentication management. 
    It also sets up the security filter chain to specify which endpoints require authentication and which are publicly accessible. 
    The JWTAuthenticationFilter is added to the filter chain to handle JWT token validation for incoming requests.
*/
public class WebSecurityConfig {

    private UserDetailsServiceImplementation userDetailsServiceImplementation;

    @Bean // Bean for JWT authentication filter
    public JWTAuthenticationFilter jwtAuthenticationFilter() {
        return new JWTAuthenticationFilter();
    }

    @Bean // Bean for password encoding using BCrypt
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean // Bean for authentication manager which is used to authenticate user credentials
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean // Bean for DAO authentication provider which uses the user details service and password encoder to authenticate users
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsServiceImplementation);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    @Bean
    /*
        This method configures the security filter chain for the application. It disables CSRF protection, defines which endpoints are publicly accessible and which require authentication,
        and adds the JWT authentication filter to the filter chain. The method returns a SecurityFilterChain object that is used by Spring Security to apply the defined security configurations.
    */
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) 
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/urls/**").authenticated()
                        .requestMatchers("/{shortUrl}").permitAll()
                        .anyRequest().authenticated()
                );

        // Set the authentication provider and add the JWT authentication filter to the filter chain
        http.authenticationProvider(authenticationProvider());
        
        // Add the JWT authentication filter before the UsernamePasswordAuthenticationFilter to ensure that JWT tokens are validated for incoming requests
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        // Build and return the SecurityFilterChain object
        return http.build();
    }
}
