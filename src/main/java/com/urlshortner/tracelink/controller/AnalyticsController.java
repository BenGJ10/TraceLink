package com.urlshortner.tracelink.controller;

import com.urlshortner.tracelink.models.UrlMapping;
import com.urlshortner.tracelink.repository.ClickEventRepository;
import com.urlshortner.tracelink.service.UrlMappingService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.urlshortner.tracelink.dto.ClickEventDTO;
import com.urlshortner.tracelink.service.UserService;
import com.urlshortner.tracelink.models.User;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/analytics")
@AllArgsConstructor
public class AnalyticsController {

    private final UrlMappingService urlMappingService;
    private final ClickEventRepository clickEventRepository;
    private final UserService userService;

    /*
        This method handles GET requests to the /{shortUrl} endpoint. It retrieves the analytics data for the specified short URL, including total clicks,
        device types, sources, and countries. The method first checks if the URL mapping exists and if the authenticated user is the owner of the URL. 
        If the mapping is valid, it constructs a response containing the analytics data and returns it with a 200 OK status. If the mapping is not found or the user is not authorized,
        it returns a 404 Not Found response.
    */
    @GetMapping("/{shortUrl}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @PathVariable String shortUrl,
            Principal principal) { // Principal object provides information about the currently authenticated user
        
        UrlMapping mapping = urlMappingService.getUrlMapping(shortUrl);
        if (mapping == null || !mapping.getUser().getUsername().equals(principal.getName())) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();

        // Get the total number of clicks for the short URL
        response.put("totalClicks", mapping.getClickCount());

        // Get the breakdown of clicks by device type, source, and country
        response.put("devices", mapListToMap(clickEventRepository.countByDeviceType(mapping)));

        // Get the breakdown of clicks by source (e.g., direct, social media, email)
        response.put("sources", mapListToMap(clickEventRepository.countBySource(mapping)));

        // Get the breakdown of clicks by country
        response.put("countries", mapListToMap(clickEventRepository.countByCountry(mapping)));

        return ResponseEntity.ok(response);
    }

    /*
        Get the daily click events for a specific short URL within a specified date range. The method takes the short URL, start date, and end date as parameters, and
        returns a list of ClickEventDTO objects representing the click events that occurred within the specified date range. 
    */
    @GetMapping("/daily/{shortUrl}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ClickEventDTO>> getUrlAnalytics(@PathVariable String shortUrl,
                                                               @RequestParam("startDate") String startDate,
                                                               @RequestParam("endDate") String endDate){
        // Parse the start and end dates from the request parameters                                                                
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);

        // Retrieve the click events for the specified short URL and date range
        List<ClickEventDTO> clickEventDTOS = urlMappingService.getClickEventsByDate(shortUrl, start, end);
        return ResponseEntity.ok(clickEventDTOS);
    }

    /*
        Get the total number of clicks for all short URLs owned by the authenticated user, grouped by date. The method takes the start date and end date as parameters, 
        and returns a map where the keys are the dates and the values are the total number of clicks for that date. 

    */
    @GetMapping("/total")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<LocalDate, Long>> getTotalClicksByDate(Principal principal,
                                                                     @RequestParam("startDate") String startDate,
                                                                     @RequestParam("endDate") String endDate){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        User user = userService.findByUsername(principal.getName());
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);
        
        // Retrieve the total number of clicks for all short URLs owned by the authenticated user, grouped by date
        Map<LocalDate, Long> totalClicks = urlMappingService.getTotalClicksByUserAndDate(user, start, end);
        return ResponseEntity.ok(totalClicks);
    }

    // Helper method to convert a list of Object arrays (result from a query) into a Map<String, Long>
    private Map<String, Long> mapListToMap(List<Object[]> results) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] result : results) {
            String key = result[0] != null ? result[0].toString() : "Unknown";
            Long value = ((Number) result[1]).longValue();
            map.put(key, value);
        }
        return map;
    }
}
