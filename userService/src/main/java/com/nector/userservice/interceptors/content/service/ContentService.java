package com.nector.userservice.interceptors.content.service;

import com.nector.userservice.interceptors.content.dto.CreateNotificationRequest;
import com.nector.userservice.interceptors.content.dto.CreateOfferRequest;
import com.nector.userservice.interceptors.content.dto.UpdateNotificationRequest;
import com.nector.userservice.interceptors.content.dto.UpdateOfferRequest;
import com.nector.userservice.interceptors.content.model.CompanyNotification;
import com.nector.userservice.interceptors.content.model.CompanyOffer;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ContentService {

    // Offers
    CompanyOffer createOffer(CreateOfferRequest request, MultipartFile image);
    List<CompanyOffer> getActiveOffers();
    List<CompanyOffer> getAllOffers();
    CompanyOffer updateOffer(Long id, UpdateOfferRequest request);
    CompanyOffer replaceOfferImage(Long id, MultipartFile image);
    CompanyOffer toggleOffer(Long id);
    void deleteOffer(Long id);

    // Notifications
    CompanyNotification createNotification(CreateNotificationRequest request);
    List<CompanyNotification> getActiveNotifications();
    List<CompanyNotification> getAllNotifications();
    CompanyNotification updateNotification(Long id, UpdateNotificationRequest request);
    CompanyNotification toggleNotification(Long id);
    void deleteNotification(Long id);
}
