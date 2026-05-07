package com.urlshortner.tracelink.models;

import lombok.Data;
import java.util.List;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class UrlMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String originalUrl;
    private String shortUrl;
    private int clickCount = 0;
    private LocalDateTime createdDate;
    private boolean isActive = true;
    private LocalDateTime expiresAt;

    // Many urls can map to a single user, many-to-one mapping
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // One particular url can have multiple click events — cascade delete them when URL is removed
    @OneToMany(mappedBy = "urlMapping", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClickEvent> clickEvents;
}