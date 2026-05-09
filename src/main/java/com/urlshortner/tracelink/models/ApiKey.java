package com.urlshortner.tracelink.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name;
    
    // The prefix used for fast DB lookups (e.g., first 8 chars of the random part)
    @Column(unique = true, nullable = false)
    private String keyPrefix;
    
    // BCrypt hash of the full API key
    @Column(nullable = false)
    private String hashedKey;
    
    // Masked key for UI display (e.g., tl_live_****abcd)
    private String maskedKey;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastUsedAt;
    
    private boolean isActive = true;
}
