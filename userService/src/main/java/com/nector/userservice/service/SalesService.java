package com.nector.userservice.service;

import com.nector.userservice.dto.sales.SalesRequest;
import com.nector.userservice.dto.sales.SalesResponse;

public interface SalesService {

    SalesResponse createSalesEntry(SalesRequest request);
}
