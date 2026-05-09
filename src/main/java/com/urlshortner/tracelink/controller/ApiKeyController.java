package com.urlshortner.tracelink.controller;

import com.urlshortner.tracelink.dto.ApiKeyCreateResponse;
import com.urlshortner.tracelink.dto.ApiKeyResponse;
import com.urlshortner.tracelink.dto.CreateApiKeyRequest;
import com.urlshortner.tracelink.models.User;
import com.urlshortner.tracelink.service.ApiKeyService;
import com.urlshortner.tracelink.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ApiKeyResponse>> getApiKeys(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        return ResponseEntity.ok(apiKeyService.getUserApiKeys(user));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiKeyCreateResponse> createApiKey(@RequestBody CreateApiKeyRequest request, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        return ResponseEntity.ok(apiKeyService.generateApiKey(user, request.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> revokeApiKey(@PathVariable Long id, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        apiKeyService.revokeApiKey(id, user);
        return ResponseEntity.noContent().build();
    }
}
