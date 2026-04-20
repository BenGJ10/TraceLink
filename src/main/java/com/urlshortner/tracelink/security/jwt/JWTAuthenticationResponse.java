package com.urlshortner.tracelink.security.jwt;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class JWTAuthenticationResponse {
    private String token;
}



