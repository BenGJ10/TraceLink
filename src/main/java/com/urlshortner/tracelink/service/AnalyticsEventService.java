package com.urlshortner.tracelink.service;

import com.urlshortner.tracelink.models.ClickEvent;
import com.urlshortner.tracelink.models.UrlMapping;
import com.urlshortner.tracelink.repository.ClickEventRepository;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AnalyticsEventService {

    private final ClickEventRepository clickEventRepository;
    private final UserAgentAnalyzer uaa; // User-Agent parser instance
    private final RestTemplate restTemplate;

    // Constructor injection for dependencies
    public AnalyticsEventService(ClickEventRepository clickEventRepository) {
        this.clickEventRepository = clickEventRepository;
        this.restTemplate = new RestTemplate();
        
        // Initialize UserAgentAnalyzer with caching for performance
        this.uaa = UserAgentAnalyzer
                .newBuilder()
                .hideMatcherLoadStats()
                .withCache(10000)
                .build();
    }

    /*
        This method processes a click event asynchronously to avoid blocking the main request thread.
        It enriches the click event with:
        1. Hashed IP for privacy
        2. Parsed User-Agent details (browser, OS, device type)
        3. Geo-location based on IP using the IP-API service
    */
    @Async // Indicates that this method should run asynchronously
    public void processClickEvent(UrlMapping urlMapping, String source, String ip, String userAgentString) {
        // Create a new ClickEvent entity and populate basic details        
        ClickEvent clickEvent = new ClickEvent();
        clickEvent.setClickDate(LocalDateTime.now());
        clickEvent.setSource(source);
        clickEvent.setUrlMapping(urlMapping);

        // Hash the IP for privacy
        clickEvent.setIpHash(hashIp(ip));

        // Parse the User-Agent string to extract browser, OS, and device type information
        if (userAgentString != null && !userAgentString.isEmpty()) {
            clickEvent.setUserAgent(userAgentString); 
            UserAgent parsedAgent = uaa.parse(userAgentString);
            
            // Extract browser and OS information
            clickEvent.setBrowser(parsedAgent.getValue(UserAgent.AGENT_NAME));
            clickEvent.setOs(parsedAgent.getValue(UserAgent.OPERATING_SYSTEM_NAME));
            
            // Determine device type based on parsed User-Agent information
            String deviceClass = parsedAgent.getValue(UserAgent.DEVICE_CLASS);
            if (deviceClass != null) {
                if (deviceClass.equals("Desktop")) {
                    clickEvent.setDeviceType("Desktop");
                } 
                else if (deviceClass.equals("Phone") || deviceClass.equals("Mobile")) {
                    clickEvent.setDeviceType("Mobile");
                } 
                else if (deviceClass.equals("Tablet")) {
                    clickEvent.setDeviceType("Tablet");
                } 
                else {
                    clickEvent.setDeviceType("Other");
                }
            } else {
                clickEvent.setDeviceType("Unknown");
            }
        }

        // Perform Geo-IP lookup to enrich the click event with country and city information
        if (ip != null && !ip.isEmpty()) {
            try {
                String apiUrl = "http://ip-api.com/json/" + ip;
                // If IP is localhost or private network, fetch the server's public IP location for testing
                if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.startsWith("192.168.")) {
                    apiUrl = "http://ip-api.com/json/";
                }
                // Make a GET request to the IP-API service and parse the response
                Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
                if (response != null && "success".equals(response.get("status"))) {
                    clickEvent.setCountry((String) response.get("country"));
                    clickEvent.setCity((String) response.get("city"));
                }
            } 
            catch (Exception e) {
                // Silently fail geo-lookup to not disrupt the process
                System.out.println("Failed to resolve Geo IP: " + e.getMessage());
            }
        }

        // Save the enriched event
        clickEventRepository.save(clickEvent);
    }

    /*
        Helper method to hash the IP address using SHA-256 for privacy. This ensures that we do not store raw IP addresses while still allowing us to identify
        unique visitors based on their hashed IP. It uses SHA-256 for strong hashing, and falls back to a simple hash code if the algorithm is not available (though this is unlikely).
    */
    private String hashIp(String ip) {
        if (ip == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ip.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch (NoSuchAlgorithmException e) {
            return String.valueOf(ip.hashCode());
        }
    }
}
