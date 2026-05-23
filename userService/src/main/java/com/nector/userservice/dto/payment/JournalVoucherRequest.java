package com.nector.userservice.dto.payment;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class JournalVoucherRequest {
    private List<JournalVoucherEntry> entries;
    private String narration;
    private LocalDateTime date;
}