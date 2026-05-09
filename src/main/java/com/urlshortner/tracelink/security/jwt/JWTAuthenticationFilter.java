package com.urlshortner.tracelink.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.urlshortner.tracelink.models.User;
import com.urlshortner.tracelink.service.ApiKeyService;
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

    @Autowired
    private ApiKeyService apiKeyService;

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
            // Get Token from the Authorization header (JWT or API Key)
            String token = jwtTokenProvider.getJwtFromHeader(request);

            if (token != null) {
                String username = null;
                
                // Check if it's an API Key (starts with tl_live_)
                if (token.startsWith("tl_live_")) {
                    java.util.Optional<User> userOpt = apiKeyService.verifyApiKey(token);
                    if (userOpt.isPresent()) {
                        username = userOpt.get().getUsername();
                    }
                } 
                // Otherwise, validate it as a JWT token
                else if (jwtTokenProvider.validateToken(token)) {
                    username = jwtTokenProvider.getUserNameFromJwtToken(token);
                }
                
                // If we successfully resolved a username, authenticate the user
                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
