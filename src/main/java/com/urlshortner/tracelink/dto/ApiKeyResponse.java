package com.urlshortner.tracelink.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApiKeyResponse {
    private Long id;
    private String name;
    private String maskedKey;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private boolean isActive;
}
