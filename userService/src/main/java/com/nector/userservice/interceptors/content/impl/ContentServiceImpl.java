package com.nector.userservice.interceptors.content.impl;

import com.nector.userservice.cloudinary.CloudinaryService;
import com.nector.userservice.interceptors.content.dto.CreateNotificationRequest;
import com.nector.userservice.interceptors.content.dto.CreateOfferRequest;
import com.nector.userservice.interceptors.content.dto.UpdateNotificationRequest;
import com.nector.userservice.interceptors.content.dto.UpdateOfferRequest;
import com.nector.userservice.interceptors.content.model.CompanyNotification;
import com.nector.userservice.interceptors.content.model.CompanyOffer;
import com.nector.userservice.interceptors.content.repository.CompanyNotificationRepository;
import com.nector.userservice.interceptors.content.repository.CompanyOfferRepository;
import com.nector.userservice.interceptors.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentServiceImpl implements ContentService {

    private final CompanyOfferRepository offerRepository;
    private final CompanyNotificationRepository notificationRepository;
    private final CloudinaryService cloudinaryService;

    // ─── Offers ───────────────────────────────────────────────────────────────

    @Override
    public CompanyOffer createOffer(CreateOfferRequest request, MultipartFile image) {
        CompanyOffer offer = new CompanyOffer();
        offer.setTitle(request.getTitle());
        offer.setDescription(request.getDescription());
        offer.setValidFrom(request.getValidFrom());
        offer.setValidTo(request.getValidTo());
        offer.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);
        offer.setCreatedBy(request.getCreatedBy());
        offer.setIsActive(true);

        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(image);
            offer.setImageUrl(imageUrl);
        }

        return offerRepository.save(offer);
    }

    @Override
    public List<CompanyOffer> getActiveOffers() {
        return offerRepository.findAllActive(LocalDateTime.now());
    }

    @Override
    public List<CompanyOffer> getAllOffers() {
        return offerRepository.findAllByOrderByDisplayOrderAscCreatedAtDesc();
    }

    @Override
    public CompanyOffer updateOffer(Long id, UpdateOfferRequest request) {
        CompanyOffer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found: " + id));

        if (request.getTitle() != null) offer.setTitle(request.getTitle());
        if (request.getDescription() != null) offer.setDescription(request.getDescription());
        if (request.getValidFrom() != null) offer.setValidFrom(request.getValidFrom());
        if (request.getValidTo() != null) offer.setValidTo(request.getValidTo());
        if (request.getDisplayOrder() != null) offer.setDisplayOrder(request.getDisplayOrder());

        return offerRepository.save(offer);
    }

    @Override
    public CompanyOffer replaceOfferImage(Long id, MultipartFile image) {
        CompanyOffer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found: " + id));

        String imageUrl = cloudinaryService.uploadImage(image);
        offer.setImageUrl(imageUrl);
        return offerRepository.save(offer);
    }

    @Override
    public CompanyOffer toggleOffer(Long id) {
        CompanyOffer offer = offerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offer not found: " + id));
        offer.setIsActive(!offer.getIsActive());
        return offerRepository.save(offer);
    }

    @Override
    public void deleteOffer(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new RuntimeException("Offer not found: " + id);
        }
        offerRepository.deleteById(id);
    }

    // ─── Notifications ────────────────────────────────────────────────────────

    @Override
    public CompanyNotification createNotification(CreateNotificationRequest request, MultipartFile image) {
        CompanyNotification notification = new CompanyNotification();
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType() != null ? request.getType() : "INFO");
        notification.setExpiresAt(request.getExpiresAt());
        notification.setCreatedBy(request.getCreatedBy());
        notification.setIsActive(true);

        if (image != null && !image.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(image);
            notification.setImageUrl(imageUrl);
        }

        return notificationRepository.save(notification);
    }

    @Override
    public CompanyNotification replaceNotificationImage(Long id, MultipartFile image) {
        CompanyNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));

        String imageUrl = cloudinaryService.uploadImage(image);
        notification.setImageUrl(imageUrl);
        return notificationRepository.save(notification);
    }

    @Override
    public List<CompanyNotification> getActiveNotifications() {
        return notificationRepository.findAllActive(LocalDateTime.now());
    }

    @Override
    public List<CompanyNotification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public CompanyNotification updateNotification(Long id, UpdateNotificationRequest request) {
        CompanyNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));

        if (request.getTitle() != null) notification.setTitle(request.getTitle());
        if (request.getMessage() != null) notification.setMessage(request.getMessage());
        if (request.getType() != null) notification.setType(request.getType());
        if (request.getExpiresAt() != null) notification.setExpiresAt(request.getExpiresAt());

        return notificationRepository.save(notification);
    }

    @Override
    public CompanyNotification toggleNotification(Long id) {
        CompanyNotification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + id));
        notification.setIsActive(!notification.getIsActive());
        return notificationRepository.save(notification);
    }

    @Override
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new RuntimeException("Notification not found: " + id);
        }
        notificationRepository.deleteById(id);
    }
}
