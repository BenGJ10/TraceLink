package com.urlshortner.tracelink.controller;

import com.urlshortner.tracelink.models.UrlMapping;
import com.urlshortner.tracelink.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/url")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;
    private final com.urlshortner.tracelink.service.UrlMappingService urlMappingService;

    // Configurable via application properties/env vars
    @Value("${app.base-url:http://localhost:8080}/q/")
    private String baseUrl;

    /*
        This method handles GET requests to the /qr/{shortUrl} endpoint. It generates a QR code for the specified short URL in the requested format (PNG or SVG) and size.
        The method constructs the redirect URL using the base URL and the short URL, retrieves the original URL associated with the short URL, and then calls the QrCodeService to generate the QR code.
        The generated QR code is returned in the response with the appropriate content type and disposition headers. If any error occurs during QR code generation, a 500 Internal Server Error response is returned.
    */
    @GetMapping("/qr/{shortUrl}")
    public ResponseEntity<byte[]> getQrCode(
            @PathVariable String shortUrl,
            @RequestParam(defaultValue = "png") String format,
            @RequestParam(defaultValue = "200") int size) {
        try {
            String redirectUrl = baseUrl + shortUrl;
            String originalUrl = "";
            
            // Retrieve the original URL associated with the short URL from the UrlMappingService
            UrlMapping mapping = urlMappingService.getUrlMapping(shortUrl);
            if (mapping != null) {
                originalUrl = mapping.getOriginalUrl();
            }
            
            // Generate the QR code using the QrCodeService
            byte[] qrCode = qrCodeService.generateQrCode(redirectUrl, originalUrl, format, size, size);

            HttpHeaders headers = new HttpHeaders();

            // Set the content type and disposition headers based on the requested format
            if ("svg".equalsIgnoreCase(format)) {
                headers.setContentType(MediaType.valueOf("image/svg+xml"));
                headers.setContentDispositionFormData("inline", shortUrl + ".svg");
            } 
            else {
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentDispositionFormData("inline", shortUrl + ".png");
            }

            return new ResponseEntity<>(qrCode, headers, HttpStatus.OK);

        } 
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
