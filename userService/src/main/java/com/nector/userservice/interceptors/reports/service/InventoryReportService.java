package com.nector.userservice.interceptors.reports.service;

import com.nector.userservice.interceptors.reports.dto.InventorySnapshotDto;
import com.nector.userservice.interceptors.reports.dto.ReportFilterRequest;

import java.util.List;

public interface InventoryReportService {
    List<InventorySnapshotDto> getSnapshot(String category);
    List<InventorySnapshotDto> getLowStock(String category);
}
