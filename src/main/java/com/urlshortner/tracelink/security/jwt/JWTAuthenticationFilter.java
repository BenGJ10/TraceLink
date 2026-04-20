package com.urlshortner.tracelink.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtils jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    /*
        This method is called for every incoming HTTP request. It checks for the presence of a JWT token in the Authorization header,
        validates it, and if valid, sets the authentication in the security context. Finally, it continues with the filter chain.

        We can use filterchains to add multiple filters in the security configuration. Each filter can perform specific tasks such as authentication, logging, etc.
     */
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try{
            // Get JWT token from the Authorization header
            String jwt = jwtTokenProvider.getJwtFromHeader(request);

            // Validate the JWT token
            if (jwt != null && jwtTokenProvider.validateToken(jwt)){
                // Get username from the token and load user details
                String username = jwtTokenProvider.getUserNameFromJwtToken(jwt);
                
                // Load user details from the database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (userDetails != null){
                    // Create an authentication token and set it in the security context
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Set the authentication in the security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
