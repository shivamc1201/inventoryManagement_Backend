package com.nector.userservice.interceptors.content;

import com.nector.userservice.interceptors.content.dto.CreateNotificationRequest;
import com.nector.userservice.interceptors.content.dto.CreateOfferRequest;
import com.nector.userservice.interceptors.content.dto.UpdateNotificationRequest;
import com.nector.userservice.interceptors.content.dto.UpdateOfferRequest;
import com.nector.userservice.interceptors.content.model.CompanyNotification;
import com.nector.userservice.interceptors.content.model.CompanyOffer;
import com.nector.userservice.interceptors.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Content", description = "APIs for Company Offers and Notifications")
public class ContentController {

    private final ContentService contentService;

    // ─── Offers ───────────────────────────────────────────────────────────────

    @PostMapping(value = "/offers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create offer", description = "Admin: create a new offer with an optional image upload")
    public ResponseEntity<CompanyOffer> createOffer(
            @RequestPart("data") CreateOfferRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        log.info("Creating offer: {}", request.getTitle());
        return ResponseEntity.ok(contentService.createOffer(request, image));
    }

    @GetMapping("/offers/active")
    @Operation(summary = "Get active offers", description = "All users: returns active, within-validity offers")
    public ResponseEntity<List<CompanyOffer>> getActiveOffers() {
        return ResponseEntity.ok(contentService.getActiveOffers());
    }

    @GetMapping("/offers/all")
    @Operation(summary = "Get all offers", description = "Admin: returns all offers including inactive")
    public ResponseEntity<List<CompanyOffer>> getAllOffers() {
        return ResponseEntity.ok(contentService.getAllOffers());
    }

    @PutMapping("/offers/{id}")
    @Operation(summary = "Update offer", description = "Admin: update offer text and dates (not image)")
    public ResponseEntity<CompanyOffer> updateOffer(
            @PathVariable Long id,
            @RequestBody UpdateOfferRequest request) {
        return ResponseEntity.ok(contentService.updateOffer(id, request));
    }

    @PatchMapping(value = "/offers/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Replace offer image", description = "Admin: replace the image of an existing offer")
    public ResponseEntity<CompanyOffer> replaceOfferImage(
            @PathVariable Long id,
            @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(contentService.replaceOfferImage(id, image));
    }

    @PatchMapping("/offers/{id}/toggle")
    @Operation(summary = "Toggle offer active status", description = "Admin: activate or deactivate an offer")
    public ResponseEntity<CompanyOffer> toggleOffer(@PathVariable Long id) {
        return ResponseEntity.ok(contentService.toggleOffer(id));
    }

    @DeleteMapping("/offers/{id}")
    @Operation(summary = "Delete offer", description = "Admin: permanently delete an offer")
    public ResponseEntity<Map<String, String>> deleteOffer(@PathVariable Long id) {
        contentService.deleteOffer(id);
        return ResponseEntity.ok(Map.of("message", "Offer deleted successfully"));
    }

    // ─── Notifications ────────────────────────────────────────────────────────

    @PostMapping(value = "/notifications", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create notification", description = "Admin: create a company notification with an optional image")
    public ResponseEntity<CompanyNotification> createNotification(
            @RequestPart("data") CreateNotificationRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        log.info("Creating notification: {}", request.getTitle());
        return ResponseEntity.ok(contentService.createNotification(request, image));
    }

    @PatchMapping(value = "/notifications/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Replace notification image", description = "Admin: replace the image of an existing notification")
    public ResponseEntity<CompanyNotification> replaceNotificationImage(
            @PathVariable Long id,
            @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(contentService.replaceNotificationImage(id, image));
    }

    @GetMapping("/notifications/active")
    @Operation(summary = "Get active notifications", description = "All users: returns active, non-expired notifications")
    public ResponseEntity<List<CompanyNotification>> getActiveNotifications() {
        return ResponseEntity.ok(contentService.getActiveNotifications());
    }

    @GetMapping("/notifications/all")
    @Operation(summary = "Get all notifications", description = "Admin: returns all notifications including inactive")
    public ResponseEntity<List<CompanyNotification>> getAllNotifications() {
        return ResponseEntity.ok(contentService.getAllNotifications());
    }

    @PutMapping("/notifications/{id}")
    @Operation(summary = "Update notification", description = "Admin: update notification content")
    public ResponseEntity<CompanyNotification> updateNotification(
            @PathVariable Long id,
            @RequestBody UpdateNotificationRequest request) {
        return ResponseEntity.ok(contentService.updateNotification(id, request));
    }

    @PatchMapping("/notifications/{id}/toggle")
    @Operation(summary = "Toggle notification active status", description = "Admin: activate or deactivate a notification")
    public ResponseEntity<CompanyNotification> toggleNotification(@PathVariable Long id) {
        return ResponseEntity.ok(contentService.toggleNotification(id));
    }

    @DeleteMapping("/notifications/{id}")
    @Operation(summary = "Delete notification", description = "Admin: permanently delete a notification")
    public ResponseEntity<Map<String, String>> deleteNotification(@PathVariable Long id) {
        contentService.deleteNotification(id);
        return ResponseEntity.ok(Map.of("message", "Notification deleted successfully"));
    }
}
