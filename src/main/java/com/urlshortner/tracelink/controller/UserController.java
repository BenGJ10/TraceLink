package com.urlshortner.tracelink.controller;

import com.urlshortner.tracelink.dto.UpdatePasswordRequest;
import com.urlshortner.tracelink.dto.UpdateUsernameRequest;
import com.urlshortner.tracelink.models.User;
import com.urlshortner.tracelink.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /*
        Method to retrieve the authenticated user's profile details. It fetches the user using the
        Principal object and returns basic account information such as username and email.
    */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> getProfile(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        Map<String, String> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        return ResponseEntity.ok(response);
    }

    /*
        Method to update the authenticated user's username. It receives an UpdateUsernameRequest containing
        the new username and attempts to update it. Returns success message or an error if already taken.
    */
    @PatchMapping("/profile/username")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateUsername(@RequestBody UpdateUsernameRequest request, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName());
            userService.updateUsername(user, request.getNewUsername());
            return ResponseEntity.ok(Map.of("message", "Username updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /*
        Method to update the authenticated user's password. It requires the current password for verification
        along with the new password. Returns a success message if successful, or an error if validation fails.
    */
    @PatchMapping("/profile/password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordRequest request, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName());
            userService.updatePassword(user, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /*
        Method to permanently delete the authenticated user's account and all associated data.
        Returns a 204 No Content response upon successful deletion.
    */
    @DeleteMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteAccount(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        userService.deleteUser(user);
        return ResponseEntity.noContent().build();
    }
}
