package com.nector.userservice.interceptors.distributor.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "Distributor response")
public class DistributorResponseDTO {
    
    @Schema(description = "Distributor ID", example = "1")
    private Long id;
    
    @Schema(description = "Distributor name", example = "ABC Distribution Ltd")
    private String name;
    
    @Schema(description = "Assigned person", example = "John Manager")
    private String assignedPerson;

    @Schema(description = "Assigned person ID", example = "1")
    private Long salespersonId;
    
    @Schema(description = "Distributor type", example = "RETAIL")
    private String distributorType;
    
    @Schema(description = "Company type", example = "PRIVATE_LIMITED")
    private String companyType;
    
    @Schema(description = "Contact email", example = "contact@abcdist.com")
    private String contactEmail;
    
    @Schema(description = "Phone number", example = "+1234567890")
    private String phoneNumber;
    
    @Schema(description = "Alternate contact", example = "+1234567891")
    private String alternateContact;
    
    @Schema(description = "Address", example = "123 Business Street, City, State")
    private String address;
    
    @Schema(description = "Aadhaar number", example = "123456789012")
    private String aadhaarNumber;
    
    @Schema(description = "PAN number", example = "ABCDE1234F")
    private String panNumber;
    
    @Schema(description = "GST number", example = "12ABCDE1234F1Z5")
    private String gstNumber;
    
    @Schema(description = "Status", example = "ACTIVE")
    private DistributorStatus status;
    
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdOn;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedOn;

    @Schema(description = "Unique username", example = "johndoe123")
    private String username;

    @Schema(description = "User email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User password", example = "pass")
    private String password;

    @Schema(description = "accountNumber", example = "11111111222")
    private String accountNumber;

    @Schema(description = "accountName", example = "Sample Name")
    private String accountName;

    @Schema(description = "IFSC", example = "SBI12345")
    private String IFSC;

}