package com.urlshortner.tracelink.controller;

import com.urlshortner.tracelink.dto.UrlMappingDTO;
import com.urlshortner.tracelink.models.User;
import com.urlshortner.tracelink.service.UrlMappingService;
import com.urlshortner.tracelink.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/urls")
@AllArgsConstructor
public class UrlMappingController {

    private UrlMappingService urlMappingService;
    private UserService userService;

    /*
        Create a new short URL mapping for the given original URL and user. The method generates a short URL, saves the mapping to the database, 
        and returns a DTO containing the mapping details. 
    */
    @PostMapping("/shorten")
    @PreAuthorize("hasRole('USER')") // Requires authentication
    public ResponseEntity<UrlMappingDTO> createShortUrl(@RequestBody Map<String, String> request, Principal principal) {
        // Extract the original URL from the request body
        String originalUrl = request.get("originalUrl");

        // Find the user based on the authenticated principal's username
        User user = userService.findByUsername(principal.getName());

        // Create a new short URL mapping using the UrlMappingService and return the resulting DTO in the response
        UrlMappingDTO urlMappingDTO = urlMappingService.createShortUrl(originalUrl, user);
        return ResponseEntity.ok(urlMappingDTO);
    }

    /*
        Retrieve all URL mappings created by the authenticated user. The method finds the user based on
        the authenticated principal's username, retrieves the list of URL mappings associated with that
        user, and returns the list of DTOs in the response.
    */
    @GetMapping("/myurls")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UrlMappingDTO>> getUserUrls(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        List<UrlMappingDTO> urls = urlMappingService.getUrlsByUser(user);
        return ResponseEntity.ok(urls);
    }

    /*
        Delete a short URL mapping. Only the owner of the URL can delete it.
        Returns 204 No Content on success, or 403 Forbidden if the user does not own the URL.
    */
    @DeleteMapping("/{shortUrl}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteUrl(@PathVariable String shortUrl, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        boolean deleted = urlMappingService.deleteUrl(shortUrl, user);
        
        if (deleted) {
            return ResponseEntity.noContent().build();
        } 
        else {
            return ResponseEntity.status(403).build();
        }
    }

    /*
        Toggle the active state of a short URL. Only the owner can toggle it. Returns the updated UrlMappingDTO, or 403 if not the owner.
    */
    @PatchMapping("/{shortUrl}/toggle-active")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UrlMappingDTO> toggleActive(@PathVariable String shortUrl, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        UrlMappingDTO updated = urlMappingService.toggleActive(shortUrl, user);
        
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        else {
            return ResponseEntity.status(403).build();
        }
    }
}
