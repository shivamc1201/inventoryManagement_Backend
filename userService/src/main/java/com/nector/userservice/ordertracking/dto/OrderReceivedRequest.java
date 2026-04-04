package com.nector.userservice.ordertracking.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OrderReceivedRequest {
    private boolean received;    // true = YES, false = NO
    private String remarks;
}
