package com.nector.userservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SalesKpiUpdateRequest {

    @NotBlank(message = "userName is required")
    private String userName;

    @NotBlank(message = "empCode is required")
    private String empCode;

    @NotBlank(message = "date is required")
    private String date;

    private Double totalDistanceInKm;

    private Integer noOfMeetings;

    // Visit/onboard counts received from 3rd party — mapped to EmployeeKpiAssignment
    private Integer farmerVisit;
    private Integer distributorVisit;
    private Integer dealerVisit;
    private Integer dealerOnboard;
    private Integer distributorOnboard;
    private Integer retailerVisit;

    @Valid
    private List<MeetingDetailRequest> meetingDetails;
}
