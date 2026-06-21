package com.nector.userservice.ordertracking.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStepDTO {
    private Long id;                    // step row id, for PATCH calls
    private Integer stepSequence;       // 1–11
    private String label;
    private String status;              // "completed"|"pending"|"cancelled"|"in-progress"
    private String deliveryBy;
    private String date;               // "yyyy-MM-dd" or null
    private String remarks;
    private AssignedPersonDTO assignedPerson;   // null if no one assigned
    private boolean hasDownload;
    private String downloadLabel;
    private boolean hasAction;
    private String actionResponse;      // "yes" | "no" | null
}
