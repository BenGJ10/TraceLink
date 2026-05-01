package com.urlshortner.tracelink.service;

import com.urlshortner.tracelink.dto.ClickEventDTO;
import com.urlshortner.tracelink.dto.UrlMappingDTO;
import com.urlshortner.tracelink.models.ClickEvent;
import com.urlshortner.tracelink.models.UrlMapping;
import com.urlshortner.tracelink.models.User;
import com.urlshortner.tracelink.repository.ClickEventRepository;
import com.urlshortner.tracelink.repository.UrlMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UrlMappingService {

    @Autowired
    private UrlMappingRepository urlMappingRepository;

    @Autowired
    private ClickEventRepository clickEventRepository;

    @Autowired
    private AnalyticsEventService analyticsEventService;

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
        urlMappingDTO.setActive(urlMapping.isActive());
        urlMappingDTO.setExpiresAt(urlMapping.getExpiresAt());
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
    
    public List<UrlMappingDTO> getUrlsByUser(User user) {
        // For every UrlMapping entity associated with the given user, convert it to a UrlMappingDTO and return the list of DTOs
        return urlMappingRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .toList();
    }

    /*
        Retrieve click events for a specific short URL within a given date range. The method finds the
        UrlMapping entity based on the provided short URL, retrieves the click events associated with that
        UrlMapping that occurred between the specified start and end dates, groups the click events by date,
        counts the number of clicks for each date, and returns a list of ClickEventDTOs containing the date and click count.
    */
    public List<ClickEventDTO> getClickEventsByDate(String shortUrl, LocalDateTime start, LocalDateTime end) {
        // Find the UrlMapping entity based on the provided short URL
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping != null) {
            return clickEventRepository.findByUrlMappingAndClickDateBetween(urlMapping, start, end).stream()
                    // Group the click events by date and count the number of clicks for each date, then map the results to ClickEventDTOs
                    .collect(Collectors.groupingBy(click -> click.getClickDate().toLocalDate(), Collectors.counting()))
                    .entrySet().stream()
                    .map(entry -> {
                        // A new ClickEventDTO for each date, setting the click date and count based on the grouped results
                        ClickEventDTO clickEventDTO = new ClickEventDTO();
                        clickEventDTO.setClickDate(entry.getKey());
                        clickEventDTO.setCount(entry.getValue());
                        return clickEventDTO;
                    })
                    .collect(Collectors.toList());
        }
        return null;
    }

    /*
        Retrieve the total number of clicks for all URL mappings associated with a specific user within a given date range. 
        The method finds all UrlMapping entities for the user, retrieves the click events associated with those UrlMappings that occurred between the specified start and end dates, 
        groups the click events by date, counts the number of clicks for each date.

    */
    public Map<LocalDate, Long> getTotalClicksByUserAndDate(User user, LocalDate start, LocalDate end) {
        // Find all UrlMapping entities for the given user
        List<UrlMapping> urlMappings = urlMappingRepository.findByUser(user);
        
        // Retrieve the click events for the specified user and date range, then group the click events by date and count the number of clicks for each date
        List<ClickEvent> clickEvents = clickEventRepository.findByUrlMappingInAndClickDateBetween(urlMappings, start.atStartOfDay(), end.plusDays(1).atStartOfDay());
        
        return clickEvents.stream()
                .collect(Collectors.groupingBy(click -> click.getClickDate().toLocalDate(), Collectors.counting()));

    }

    /*
        Retrieve the original URL associated with a given short URL. The method finds the UrlMapping entity based on the provided short URL, 
        increments the click count for that UrlMapping, saves the updated UrlMapping back to the database, creates a new ClickEvent entity to record the click event, 
        and returns the UrlMapping entity containing the original URL and other details.
    */
    public UrlMapping getOriginalUrl(String shortUrl, String source, String ip, String userAgent) {
        // Retrieve the UrlMapping entity based on the provided short URL
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        
        if (urlMapping != null) {
            // Increment the click count for the UrlMapping, save the updated UrlMapping back to the database
            urlMapping.setClickCount(urlMapping.getClickCount() + 1);
            urlMappingRepository.save(urlMapping);
            
            // Delegate the event processing (GeoIP, UserAgent parsing, Saving) to the Async service
            analyticsEventService.processClickEvent(urlMapping, source, ip, userAgent);
        }
        return urlMapping;
    }

    public UrlMapping getUrlMapping(String shortUrl) {
        return urlMappingRepository.findByShortUrl(shortUrl);
    }

    /*
        Delete a URL mapping owned by the given user. Returns true if deleted, false if not found or not owned by user.
    */
    public boolean deleteUrl(String shortUrl, User user) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping != null && urlMapping.getUser().getId().equals(user.getId())) {
            urlMappingRepository.delete(urlMapping);
            return true;
        }
        return false;
    }

    /*
        Toggle the isActive state of a URL mapping. Returns the updated DTO, or null if not found / not owned.
    */
    public UrlMappingDTO toggleActive(String shortUrl, User user) {
        UrlMapping urlMapping = urlMappingRepository.findByShortUrl(shortUrl);
        if (urlMapping != null && urlMapping.getUser().getId().equals(user.getId())) {
            urlMapping.setActive(!urlMapping.isActive());
            urlMappingRepository.save(urlMapping);
            return convertToDto(urlMapping);
        }
        return null;
    }
}
