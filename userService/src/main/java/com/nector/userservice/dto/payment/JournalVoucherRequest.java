package com.nector.userservice.dto.payment;

import lombok.Data;
import java.util.List;

@Data
public class JournalVoucherRequest {
    private List<JournalVoucherEntry> entries;
    private String narration;
}