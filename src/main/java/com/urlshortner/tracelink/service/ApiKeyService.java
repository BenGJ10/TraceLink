package com.urlshortner.tracelink.service;

import com.urlshortner.tracelink.dto.ApiKeyCreateResponse;
import com.urlshortner.tracelink.dto.ApiKeyResponse;
import com.urlshortner.tracelink.models.ApiKey;
import com.urlshortner.tracelink.models.User;
import com.urlshortner.tracelink.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    /*
        Generates a secure API key for the user.
        Format: tl_live_ + 32 bytes of secure random (Base64 URL safe without padding)
        Stores the hashed version and a lookup prefix.
    */
    public ApiKeyCreateResponse generateApiKey(User user, String name) {
        // 1. Generate 32 secure random bytes
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String randomPart = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        
        // 2. Construct the full raw key
        String rawKey = "tl_live_" + randomPart;
        
        // 3. Extract the prefix for fast DB lookups (first 8 characters of random part)
        String keyPrefix = randomPart.substring(0, 8);
        
        // 4. Create the masked key for display
        String maskedKey = "tl_live_****" + randomPart.substring(randomPart.length() - 4);
        
        // 5. Hash the full key
        String hashedKey = passwordEncoder.encode(rawKey);
        
        ApiKey apiKey = new ApiKey();
        apiKey.setUser(user);
        apiKey.setName(name != null && !name.trim().isEmpty() ? name : "Default Key");
        apiKey.setKeyPrefix(keyPrefix);
        apiKey.setHashedKey(hashedKey);
        apiKey.setMaskedKey(maskedKey);
        
        ApiKey saved = apiKeyRepository.save(apiKey);
        
        return new ApiKeyCreateResponse(mapToResponse(saved), rawKey);
    }

    public List<ApiKeyResponse> getUserApiKeys(User user) {
        return apiKeyRepository.findByUserOrderByIdDesc(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void revokeApiKey(Long id, User user) {
        ApiKey apiKey = apiKeyRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("API Key not found"));
        apiKey.setActive(false);
        apiKeyRepository.save(apiKey);
    }

    /*
        Used by the authentication filter to verify a Bearer API token.
    */
    public Optional<User> verifyApiKey(String rawKey) {
        if (rawKey == null || !rawKey.startsWith("tl_live_") || rawKey.length() < 20) {
            return Optional.empty();
        }
        
        // Extract the prefix
        String randomPart = rawKey.substring(8);
        String keyPrefix = randomPart.substring(0, 8);
        
        // Lookup active keys by prefix
        Optional<ApiKey> apiKeyOpt = apiKeyRepository.findByKeyPrefixAndIsActiveTrue(keyPrefix);
        
        if (apiKeyOpt.isPresent()) {
            ApiKey apiKey = apiKeyOpt.get();
            // Verify the bcrypt hash
            if (passwordEncoder.matches(rawKey, apiKey.getHashedKey())) {
                // Update last used at
                apiKey.setLastUsedAt(LocalDateTime.now());
                apiKeyRepository.save(apiKey);
                return Optional.of(apiKey.getUser());
            }
        }
        return Optional.empty();
    }

    private ApiKeyResponse mapToResponse(ApiKey key) {
        ApiKeyResponse response = new ApiKeyResponse();
        response.setId(key.getId());
        response.setName(key.getName());
        response.setMaskedKey(key.getMaskedKey());
        response.setCreatedAt(key.getCreatedAt());
        response.setLastUsedAt(key.getLastUsedAt());
        response.setActive(key.isActive());
        return response;
    }
}
