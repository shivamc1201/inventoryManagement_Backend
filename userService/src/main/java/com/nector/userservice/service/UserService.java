package com.nector.userservice.service;

import com.nector.userservice.common.UserStatus;
import com.nector.userservice.common.UserUpdateRequest;
import com.nector.userservice.model.User;
import com.nector.userservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public User updateUser(Long userId, UserUpdateRequest req) {
        log.info("Entering updateUser() for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Exiting updateUser() - User not found: {}", userId);
                    return new EntityNotFoundException("User not found");
                });

        Optional.ofNullable(req.getUsername()).ifPresent(user::setUsername);
        Optional.ofNullable(req.getEmail()).ifPresent(user::setEmail);
        Optional.ofNullable(req.getStatus()).ifPresent(user::setStatus);
        Optional.ofNullable(req.getFirstName()).ifPresent(user::setFirstName);
        Optional.ofNullable(req.getLastName()).ifPresent(user::setLastName);
        Optional.ofNullable(req.getContactNo()).ifPresent(user::setContactNo);
        Optional.ofNullable(req.getAlternateContactNo()).ifPresent(user::setAlternateContactNo);
        Optional.ofNullable(req.getBloodGroup()).ifPresent(user::setBloodGroup);
        Optional.ofNullable(req.getCompleteAddress()).ifPresent(user::setCompleteAddress);
        Optional.ofNullable(req.getCity()).ifPresent(user::setCity);
        Optional.ofNullable(req.getDateOfBirth()).ifPresent(user::setDateOfBirth);
        Optional.ofNullable(req.getGender()).ifPresent(user::setGender);
        Optional.ofNullable(req.getCountry()).ifPresent(user::setCountry);
        Optional.ofNullable(req.getZip()).ifPresent(user::setZip);
        Optional.ofNullable(req.getRoleType()).ifPresent(user::setRoleType);

        User updatedUser = userRepository.save(user);
        log.info("Exiting updateUser() - User updated successfully for userId: {}", userId);
        return updatedUser;
    }

    public User suspendUser(Long userId){
        log.info("Entering updateUser() for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Exiting updateUser() - User not found: {}", userId);
                    return new EntityNotFoundException("User not found");
                });
        user.setStatus(UserStatus.SUSPENDED);

        User updatedUser = userRepository.save(user);
        log.info("Exiting updateUser() - User updated successfully for userId: {}", userId);
        return updatedUser;
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Entering deleteUser() for userId: {}", userId);
        
        if (!userRepository.existsById(userId)) {
            log.warn("Exiting deleteUser() - User not found: {}", userId);
            throw new EntityNotFoundException("User not found");
        }
        // Delete related records first
        userRepository.deleteUserApprovalsByUserId(userId);
        // Then delete the user
        userRepository.deleteById(userId);
        log.info("Exiting deleteUser() - User deleted successfully for userId: {}", userId);
    }

}
