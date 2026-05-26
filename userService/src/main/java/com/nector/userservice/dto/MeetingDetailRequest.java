package com.nector.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MeetingDetailRequest {

    @NotBlank(message = "clientName is required")
    private String clientName;

    private String contactPerson;

    private String clientContactNo;

    private String clientEmail;

    private String type;

    private String meetingAddress;

    private String createDate;

    private String createTime;
}
