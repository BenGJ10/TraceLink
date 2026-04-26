package com.urlshortner.tracelink.service;

import com.urlshortner.tracelink.dto.UrlMappingDTO;
import com.urlshortner.tracelink.models.UrlMapping;
import com.urlshortner.tracelink.models.User;
import com.urlshortner.tracelink.repository.ClickEventRepository;
import com.urlshortner.tracelink.repository.UrlMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UrlMappingService {

    @Autowired
    private UrlMappingRepository urlMappingRepository;
//    private ClickEventRepository clickEventRepository;

    /*
        Create a new short URL mapping for the given original URL and user. The method generates a
        short URL, saves the mapping to the database, and returns a DTO containing the mapping details.
     */
    public UrlMappingDTO createShortUrl(String originalUrl, User user) {
        String shortUrl = generateShortUrl();
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setOriginalUrl(originalUrl);
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setUser(user);
        urlMapping.setCreatedDate(LocalDateTime.now());
        UrlMapping savedUrlMapping = urlMappingRepository.save(urlMapping);
        return convertToDto(savedUrlMapping);
    }

    /*
        Convert a UrlMapping entity to a UrlMappingDTO. This method extracts the relevant fields from the
        UrlMapping entity and populates a UrlMappingDTO object, which is then returned.
    */
    private UrlMappingDTO convertToDto(UrlMapping urlMapping){
        UrlMappingDTO urlMappingDTO = new UrlMappingDTO();
        urlMappingDTO.setId(urlMapping.getId());
        urlMappingDTO.setOriginalUrl(urlMapping.getOriginalUrl());
        urlMappingDTO.setShortUrl(urlMapping.getShortUrl());
        urlMappingDTO.setClickCount(urlMapping.getClickCount());
        urlMappingDTO.setCreatedDate(urlMapping.getCreatedDate());
        urlMappingDTO.setUsername(urlMapping.getUser().getUsername());
        return urlMappingDTO;
    }
    
    /*
        Generate a random short URL string consisting of 8 characters. The method uses a combination of
        uppercase letters, lowercase letters, and digits to create a unique short URL.
        This will ensure that the generated short URL is sufficiently random and has a low probability of collisions, making it suitable for use in a URL shortening service.
    */
    private String generateShortUrl() {
        // Define the characters that can be used in the short URL (uppercase, lowercase, digits)
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        
        // Use a StringBuilder to construct the short URL and a Random instance to generate random indices
        Random random = new Random();
        StringBuilder shortUrl = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            shortUrl.append(characters.charAt(random.nextInt(characters.length())));
        }
        return shortUrl.toString();
    }
}
