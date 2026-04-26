package com.urlshortner.tracelink.controller;

import com.urlshortner.tracelink.dto.UrlMappingDTO;
import com.urlshortner.tracelink.models.User;
import com.urlshortner.tracelink.service.UrlMappingService;
import com.urlshortner.tracelink.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/urls")
@AllArgsConstructor
public class UrlMappingController {

    private UrlMappingService urlMappingService;
    private UserService userService;
    
    /*
        Create a new short URL mapping for the given original URL and user. The method generates a
        short URL, saves the mapping to the database, and returns a DTO containing the mapping details.        
    */
    @PostMapping("/shorten") 
    @PreAuthorize("hasRole('USER')") // Requires authentication
    public ResponseEntity<UrlMappingDTO> createShortUrl(@RequestBody Map<String, String> request, Principal principal){
        // Extract the original URL from the request body
        String originalUrl = request.get("originalUrl"); 
        
        // Find the user based on the authenticated principal's username
        User user = userService.findByUsername(principal.getName());
        
        // Create a new short URL mapping using the UrlMappingService and return the resulting DTO in the response
        UrlMappingDTO urlMappingDTO = urlMappingService.createShortUrl(originalUrl, user);
        return ResponseEntity.ok(urlMappingDTO);
    }
}
