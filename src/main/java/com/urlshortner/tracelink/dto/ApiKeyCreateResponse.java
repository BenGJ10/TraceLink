package com.urlshortner.tracelink.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiKeyCreateResponse {
    private ApiKeyResponse keyDetails;
    private String rawKey; // Provided ONLY once upon creation
}
