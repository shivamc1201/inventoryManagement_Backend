package com.nector.userservice.controller;

import com.nector.userservice.dto.CustomResponse;
import com.nector.userservice.dto.MeetingDetailRequest;
import com.nector.userservice.dto.SalesKpiUpdateRequest;
import com.nector.userservice.service.SalesKpiUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sales-kpi")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sales KPI Update", description = "3rd party API to push daily sales KPI data")
public class SalesKpiUpdateController {

    private final SalesKpiUpdateService salesKpiUpdateService;

    @PostMapping("/update")
    @Operation(summary = "Save Sales KPI Update", description = "Accepts daily salesperson KPI data from 3rd party and persists it")
    public ResponseEntity<CustomResponse> update(@Valid @RequestBody List<SalesKpiUpdateRequest> requests) {
        log.info("KRA /update received {} record(s) from external source", requests == null ? 0 : requests.size());

        if (requests != null) {
            for (int i = 0; i < requests.size(); i++) {
                SalesKpiUpdateRequest r = requests.get(i);
                int meetingCount = r.getMeetingDetails() == null ? 0 : r.getMeetingDetails().size();
                log.info("  [{}] empCode={} userName={} date={} distanceKm={} noOfMeetings={} meetingDetailsCount={}",
                        i, r.getEmpCode(), r.getUserName(), r.getDate(),
                        r.getTotalDistanceInKm(), r.getNoOfMeetings(), meetingCount);

                if (r.getMeetingDetails() != null) {
                    for (int j = 0; j < r.getMeetingDetails().size(); j++) {
                        MeetingDetailRequest md = r.getMeetingDetails().get(j);
                        log.info("    [{}][{}] type={} clientName={} contactPerson={} clientContactNo={} clientEmail={} meetingAddress={} createDate={} createTime={}",
                                i, j, md.getType(), md.getClientName(), md.getContactPerson(),
                                md.getClientContactNo(), md.getClientEmail(),
                                md.getMeetingAddress(), md.getCreateDate(), md.getCreateTime());
                    }
                }
            }
        }

        CustomResponse response = salesKpiUpdateService.saveAll(requests);
        log.info("KRA /update completed. Response: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recalculate")
    @Operation(
        summary = "Recalculate KPI achievements for a month",
        description = "Resets all KPI achieved values for the given month and replays stored meeting details to recompute. " +
                      "Defaults to current month if month/year not supplied. Safe to call multiple times."
    )
    public ResponseEntity<Map<String, Object>> recalculate(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        LocalDate ref = (month == null || year == null) ? LocalDate.now() : LocalDate.of(year, month, 1);
        int m = ref.getMonthValue();
        int y = ref.getYear();
        log.info("KPI recalculate triggered for {}/{}", m, y);
        Map<String, Object> result = salesKpiUpdateService.recalculateForMonth(m, y);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/deduplicate")
    @Operation(
        summary = "Remove duplicate sales KPI rows for a month",
        description = "Deletes extra rows in sales_KPI_update that share the same empCode+date, " +
                      "keeping the oldest (lowest id) record. Run this before /recalculate to get correct KPI values. " +
                      "Defaults to current month if month/year not supplied."
    )
    public ResponseEntity<Map<String, Object>> deduplicate(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        LocalDate ref = (month == null || year == null) ? LocalDate.now() : LocalDate.of(year, month, 1);
        int m = ref.getMonthValue();
        int y = ref.getYear();
        log.info("KPI deduplicate triggered for {}/{}", m, y);
        Map<String, Object> result = salesKpiUpdateService.deduplicateMonth(m, y);
        return ResponseEntity.ok(result);
    }
}
