package com.nector.userservice.interceptors.forgotPassword.impl;

import com.nector.userservice.interceptors.distributor.repository.DistributorRepository;
import com.nector.userservice.interceptors.forgotPassword.model.ForgotPasswordRequest;
import com.nector.userservice.interceptors.forgotPassword.model.ForgotPasswordResponse;
import com.nector.userservice.interceptors.forgotPassword.service.ForgotPasswordService;
import com.nector.userservice.repository.SalesPersonRepository;
import com.nector.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final UserRepository userRepository;
    private final DistributorRepository distributorRepository;
    private final SalesPersonRepository salesPersonRepository;

    @Override
    public ForgotPasswordResponse processForgotPassword(String username, ForgotPasswordRequest request) {
        log.info("Processing forgot password for username: {}", username);

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New password and confirm password do not match");
        }

        // Try User table first
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            var user = userOpt.get();
            if (user.getPassword().equals(request.getNewPassword())) {
                throw new RuntimeException("New password cannot be same as old password");
            }
            user.setPassword(request.getNewPassword());
            userRepository.save(user);
            log.info("Password updated successfully for user: {}", username);
            return new ForgotPasswordResponse("Password updated successfully", username, "SUCCESS");
        }

        // Try Distributor table
        var distributorOpt = distributorRepository.findByUsername(username);
        if (distributorOpt.isPresent()) {
            var distributor = distributorOpt.get();
            if (distributor.getPassword().equals(request.getNewPassword())) {
                throw new RuntimeException("New password cannot be same as old password");
            }
            distributor.setPassword(request.getNewPassword());
            distributorRepository.save(distributor);
            log.info("Password updated successfully for distributor: {}", username);
            return new ForgotPasswordResponse("Password updated successfully", username, "SUCCESS");
        }

        // Try SalesPerson table
        var salesPersonOpt = salesPersonRepository.findByUsername(username);
        if (salesPersonOpt.isPresent()) {
            var salesPerson = salesPersonOpt.get();
            if (salesPerson.getPassword().equals(request.getNewPassword())) {
                throw new RuntimeException("New password cannot be same as old password");
            }
            salesPerson.setPassword(request.getNewPassword());
            salesPersonRepository.save(salesPerson);
            log.info("Password updated successfully for salesperson: {}", username);
            return new ForgotPasswordResponse("Password updated successfully", username, "SUCCESS");
        }

        throw new RuntimeException("Username not found");
    }
}