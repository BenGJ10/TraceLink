package com.urlshortner.tracelink.models;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class ClickEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime clickDate;
    private String source; // e.g., "LINK", "QR"
    
    // Enriched Analytics Data
    private String ipHash;
    private String userAgent;
    private String browser;
    private String os;
    private String deviceType;
    private String country;
    private String city;
    @ManyToOne
    @JoinColumn(name = "url_mapping_id")
    private UrlMapping urlMapping;
}
