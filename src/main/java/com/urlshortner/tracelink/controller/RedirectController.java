package com.urlshortner.tracelink.controller;

import com.urlshortner.tracelink.models.UrlMapping;
import com.urlshortner.tracelink.service.UrlMappingService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestController
@AllArgsConstructor
public class RedirectController {

    private UrlMappingService urlMappingService;

    /*
        This method handles GET requests to the /{shortUrl} endpoint. It retrieves the original URL associated with the provided short URL
        from the UrlMappingService. If a mapping is found, it constructs an HTTP response with a 302 status code and sets the "Location" header to the original URL,
        effectively redirecting the client to the original URL. If no mapping is found, it returns a 404 Not Found response.
     */
    @GetMapping("/{shortUrl}")
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl, HttpServletRequest request){
        
        // Extract the User-Agent and IP address from the request headers for logging and analytics purposes        
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }

        // Retrieve the original URL associated with the provided short URL
        UrlMapping urlMapping = urlMappingService.getOriginalUrl(shortUrl, "LINK", ip, userAgent);

        if (urlMapping != null) {
            
            // Check if link is disabled or expired — return 410 Gone
            if (!urlMapping.isActive()) {
                return ResponseEntity.status(410).build();
            }
            if (urlMapping.getExpiresAt() != null && LocalDateTime.now().isAfter(urlMapping.getExpiresAt())) {
                return ResponseEntity.status(410).build();
            }
            
            // Construct an HTTP response with a 302 status code and set the "Location" header to the original URL
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Location", urlMapping.getOriginalUrl());
            return ResponseEntity.status(302).headers(httpHeaders).build();
        } 
        else {
            return ResponseEntity.notFound().build();
        }
    }

    // This method handles GET requests to the /q/{shortUrl} endpoint. It performs the same logic as the redirect method but is specifically designed for QR code redirection.    
    @GetMapping("/q/{shortUrl}")
    public ResponseEntity<Void> redirectQr(@PathVariable String shortUrl, HttpServletRequest request){
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }

        UrlMapping urlMapping = urlMappingService.getOriginalUrl(shortUrl, "QR", ip, userAgent);

        if (urlMapping != null) {
            if (!urlMapping.isActive()) {
                return ResponseEntity.status(410).build();
            }
            if (urlMapping.getExpiresAt() != null && LocalDateTime.now().isAfter(urlMapping.getExpiresAt())) {
                return ResponseEntity.status(410).build();
            }
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Location", urlMapping.getOriginalUrl());
            return ResponseEntity.status(302).headers(httpHeaders).build();
        } 
        else {
            return ResponseEntity.notFound().build();
        }
    }
}
