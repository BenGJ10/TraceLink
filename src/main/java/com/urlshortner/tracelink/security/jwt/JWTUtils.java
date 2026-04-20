package com.urlshortner.tracelink.security.jwt;

import com.urlshortner.tracelink.service.UserDetailsImplementation;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JWTUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    
    /*
        This method retrieves the JWT token from the "Authorization" header of the incoming HTTP request. 
        It checks if the header is present and starts with "Bearer ", which is a common convention for passing JWT tokens in HTTP headers. 
        If the token is found, it extracts and returns the token string; otherwise, it returns null.
    */
    public String getJwtFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }


    /*
        This method generates a JWT token for the authenticated user. It takes a UserDetailsImplementation object as input, which contains the user's information and authorities
        (roles). The method extracts the username and roles from the UserDetailsImplementation object, and then uses the Jwts.builder() to create a JWT token.
        The token includes the username as the subject and the roles as a claim. It also sets the issued date and expiration date for the token, and signs it using the secret key.
        Finally, it compacts the token into a string format and returns it.
    */
    public String generateToken(UserDetailsImplementation userDetails){
        String username = userDetails.getUsername();
        String roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.joining(","));
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime() + jwtExpirationMs)))
                .signWith(key())
                .compact();
    }   


    // Method to create a Key object from the JWT secret string
    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }


    /*
        This method extracts the username from the JWT token. It uses the Jwts.parser() to parse the token and verify its signature using the secret key. 
        If the token is valid, it retrieves the claims (payload) from the token and returns the subject, which is typically the username of the authenticated user.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }


    /*
        This method validates the JWT token by parsing it and verifying its signature using the secret key. 
        If the token is valid, it returns true; otherwise, it catches any exceptions that occur during parsing (such as JwtException or IllegalArgumentException) and throws a RuntimeException with the error message.
    */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith((SecretKey) key())
                    .build().parseSignedClaims(authToken);
            return true;
        } 
        catch (JwtException e) {
            throw new RuntimeException(e);
        } 
        catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } 
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
