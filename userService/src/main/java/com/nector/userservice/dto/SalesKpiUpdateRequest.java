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

    @Valid
    private List<MeetingDetailRequest> meetingDetails;
}
