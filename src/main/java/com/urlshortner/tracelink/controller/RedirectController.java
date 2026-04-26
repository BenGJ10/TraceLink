package com.urlshortner.tracelink.controller;

import com.urlshortner.tracelink.models.UrlMapping;
import com.urlshortner.tracelink.service.UrlMappingService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Void> redirect(@PathVariable String shortUrl){
        // Retrieve the original URL associated with the provided short URL
        UrlMapping urlMapping = urlMappingService.getOriginalUrl(shortUrl);
        
        if (urlMapping != null) {
            // Construct an HTTP response with a 302 status code and set the "Location" header to the original URL
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Location", urlMapping.getOriginalUrl());
            
            // Return a 302 Found response with the "Location" header set to the original URL
            return ResponseEntity.status(302).headers(httpHeaders).build();
        } 
        else {
            return ResponseEntity.notFound().build();
        }
    }
}
