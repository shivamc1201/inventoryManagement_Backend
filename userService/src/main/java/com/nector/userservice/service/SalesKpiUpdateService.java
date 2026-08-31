package com.nector.userservice.service;

import com.nector.userservice.dto.CustomResponse;
import com.nector.userservice.dto.MeetingDetailRequest;
import com.nector.userservice.dto.SalesKpiUpdateRequest;
import com.nector.userservice.enums.KPIStatus;
import com.nector.userservice.model.EmployeeKpiAssignment;
import com.nector.userservice.model.SalesKpiMeetingDetail;
import com.nector.userservice.model.SalesKpiUpdate;
import com.nector.userservice.model.SalesPerson;
import com.nector.userservice.repository.EmployeeKpiAssignmentRepository;
import com.nector.userservice.repository.SalesKpiUpdateRepository;
import com.nector.userservice.repository.SalesPersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesKpiUpdateService {

    private final SalesKpiUpdateRepository salesKpiUpdateRepository;
    private final SalesPersonRepository salesPersonRepository;
    private final EmployeeKpiAssignmentRepository assignmentRepository;

    /**
     * Maps the third-party meeting `type` field value to the exact kpi_master.kpi_name.
     * Adjust the right-hand values if your kpi_master uses different names.
     */
    private static final Map<String, String> MEETING_TYPE_TO_KPI_NAME = new HashMap<>();
    static {
        MEETING_TYPE_TO_KPI_NAME.put("Farmer",               "Farmer Visit");
        MEETING_TYPE_TO_KPI_NAME.put("Dealer",               "Dealer Visit");
        MEETING_TYPE_TO_KPI_NAME.put("Distributor",          "Distributor Visit");
        MEETING_TYPE_TO_KPI_NAME.put("Retailer",             "Retailer Visit");
        MEETING_TYPE_TO_KPI_NAME.put("Dealer Onboard",       "Dealer Onboard");
        MEETING_TYPE_TO_KPI_NAME.put("Distributor Onboard",  "Distributor Onboard");
    }

    @Transactional
    public CustomResponse saveAll(List<SalesKpiUpdateRequest> requests) {
        List<SalesKpiUpdate> saved = new ArrayList<>();

        for (SalesKpiUpdateRequest req : requests) {
            LocalDate entryDate = LocalDate.parse(req.getDate());

            // App-level fast-path check (not race-condition-safe on its own)
            if (salesKpiUpdateRepository.existsByEmpCodeAndDate(req.getEmpCode(), entryDate)) {
                log.warn("Duplicate skipped (pre-check) empCode={} date={}", req.getEmpCode(), req.getDate());
                continue;
            }

            SalesKpiUpdate entity = new SalesKpiUpdate();
            entity.setUserName(req.getUserName());
            entity.setEmpCode(req.getEmpCode());
            entity.setDate(entryDate);
            entity.setTotalDistanceInKm(req.getTotalDistanceInKm());
            entity.setNoOfMeetings(req.getNoOfMeetings());

            Optional<SalesPerson> spOpt = salesPersonRepository.findByEmployeeRollNo(req.getEmpCode());
            Long salesPersonId = null;
            if (spOpt.isPresent()) {
                entity.setSalesPerson(spOpt.get());
                salesPersonId = spOpt.get().getId();
            } else {
                log.warn("No SalesPerson found for empCode: {}", req.getEmpCode());
            }

            if (req.getMeetingDetails() != null) {
                for (MeetingDetailRequest mdReq : req.getMeetingDetails()) {
                    SalesKpiMeetingDetail detail = new SalesKpiMeetingDetail();
                    detail.setSalesKpiUpdate(entity);
                    detail.setSalesPersonId(salesPersonId);
                    detail.setClientName(mdReq.getClientName());
                    detail.setContactPerson(mdReq.getContactPerson());
                    detail.setClientContactNo(mdReq.getClientContactNo());
                    detail.setClientEmail(mdReq.getClientEmail());
                    detail.setType(mdReq.getType());
                    detail.setMeetingAddress(mdReq.getMeetingAddress());
                    detail.setCreateDate(mdReq.getCreateDate());
                    detail.setCreateTime(mdReq.getCreateTime());
                    entity.getMeetingDetails().add(detail);
                }
            }

            try {
                saved.add(salesKpiUpdateRepository.saveAndFlush(entity));
            } catch (DataIntegrityViolationException e) {
                // DB unique constraint on (emp_code, date) caught a concurrent duplicate
                log.warn("Duplicate rejected by DB constraint empCode={} date={}", req.getEmpCode(), req.getDate());
                continue;
            }

            // Update KPI achievements AFTER saving so meeting details are persisted
            if (spOpt.isPresent()) {
                updateKpiFromMeetingDetails(spOpt.get().getId(), req.getMeetingDetails());
            }
        }

        return CustomResponse.success(saved, "Sales KPI data saved successfully");
    }

    /**
     * Count meeting details by their `type` field and increment the matching
     * active KPI assignment's achievedValue.
     */
    private void updateKpiFromMeetingDetails(Long salesPersonId, List<MeetingDetailRequest> meetingDetails) {
        if (meetingDetails == null || meetingDetails.isEmpty()) {
            log.warn("No meeting details for salesPersonId={}, skipping KPI update", salesPersonId);
            return;
        }

        // Count meetings per type, ignoring unmapped types (NA, LIVE TRACKING, etc.)
        Map<String, Long> typeCounts = meetingDetails.stream()
                .filter(md -> md.getType() != null && MEETING_TYPE_TO_KPI_NAME.containsKey(md.getType()))
                .collect(Collectors.groupingBy(MeetingDetailRequest::getType, Collectors.counting()));

        if (typeCounts.isEmpty()) {
            log.info("No mappable meeting types for salesPersonId={}", salesPersonId);
            return;
        }

        for (Map.Entry<String, Long> entry : typeCounts.entrySet()) {
            String kpiName = MEETING_TYPE_TO_KPI_NAME.get(entry.getKey());
            incrementActiveKpiAchievement(salesPersonId, kpiName, BigDecimal.valueOf(entry.getValue()));
        }
    }

    private void incrementActiveKpiAchievement(Long salesPersonId, String kpiName, BigDecimal increment) {
        List<EmployeeKpiAssignment> assignments =
                assignmentRepository.findActiveByEmployeeIdAndKpiName(salesPersonId, kpiName);

        if (assignments.isEmpty()) {
            log.warn("No active KPI assignment for salesPersonId={} kpiName='{}'", salesPersonId, kpiName);
            return;
        }

        for (EmployeeKpiAssignment a : assignments) {
            a.updateAchievedValue(a.getAchievedValue().add(increment));
            assignmentRepository.save(a);
        }
    }

    /**
     * Deletes duplicate rows in sales_KPI_update for a given month, keeping the
     * lowest-id row per (empCode, date). Cascade-deletes their meeting details.
     * Returns counts of duplicates removed per empCode for auditability.
     */
    @Transactional
    public Map<String, Object> deduplicateMonth(int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Long> duplicateIds = salesKpiUpdateRepository.findDuplicateIds(startDate, endDate);
        if (duplicateIds.isEmpty()) {
            log.info("No duplicates found for {}/{}", month, year);
            return Map.of("month", month, "year", year, "duplicatesRemoved", 0);
        }

        log.info("Removing {} duplicate sales_KPI_update rows for {}/{}", duplicateIds.size(), month, year);
        // Delete children first (FK constraint), then parents
        salesKpiUpdateRepository.deleteMeetingDetailsByUpdateIds(duplicateIds);
        salesKpiUpdateRepository.deleteByIds(duplicateIds);

        return Map.of("month", month, "year", year, "duplicatesRemoved", duplicateIds.size());
    }

    /**
     * Recalculates achieved values for all KPI assignments in a given month by:
     * 1. Resetting all assignments' achievedValue to 0
     * 2. Replaying stored meeting details — using only the canonical (lowest-id) row
     *    per (empCode, date) to guard against any remaining duplicates.
     *
     * Safe to call multiple times (idempotent). Call deduplicateMonth first to
     * permanently clean up duplicates, then recalculate to get correct KPI values.
     */
    @Transactional
    public Map<String, Object> recalculateForMonth(int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // Step 1: Fetch all rows ordered by id ASC, then deduplicate in-memory
        // keeping only the first (lowest id) row per empCode+date.
        List<SalesKpiUpdate> rawUpdates =
                salesKpiUpdateRepository.findByDateBetweenWithDetails(startDate, endDate);

        Map<String, SalesKpiUpdate> canonical = new LinkedHashMap<>();
        for (SalesKpiUpdate u : rawUpdates) {
            canonical.putIfAbsent(u.getEmpCode() + "|" + u.getDate(), u);
        }
        List<SalesKpiUpdate> updates = new ArrayList<>(canonical.values());

        // employeeId -> (kpiName -> total count)
        Map<Long, Map<String, Long>> employeeKpiCounts = new HashMap<>();
        int processed = 0, skipped = 0;

        for (SalesKpiUpdate update : updates) {
            SalesPerson sp = update.getSalesPerson();
            if (sp == null) {
                Optional<SalesPerson> spOpt = salesPersonRepository.findByEmployeeRollNo(update.getEmpCode());
                if (spOpt.isEmpty()) {
                    log.warn("Recalculate: no SalesPerson for empCode={}, skipping", update.getEmpCode());
                    skipped++;
                    continue;
                }
                sp = spOpt.get();
                update.setSalesPerson(sp);
                salesKpiUpdateRepository.save(update);
            }
            Long spId = sp.getId();
            Map<String, Long> kpiCounts = employeeKpiCounts.computeIfAbsent(spId, k -> new HashMap<>());

            update.getMeetingDetails().stream()
                    .filter(md -> md.getType() != null && MEETING_TYPE_TO_KPI_NAME.containsKey(md.getType()))
                    .forEach(md -> {
                        String kpiName = MEETING_TYPE_TO_KPI_NAME.get(md.getType());
                        kpiCounts.merge(kpiName, 1L, Long::sum);
                    });
            processed++;
        }

        // Step 2: Reset all assignments for the month to zero
        List<EmployeeKpiAssignment> allAssignments =
                assignmentRepository.findAllByMonthAndYear(month, year);
        for (EmployeeKpiAssignment a : allAssignments) {
            a.setAchievedValue(BigDecimal.ZERO);
            a.setScorePercentage(BigDecimal.ZERO);
            a.setWeightedScore(BigDecimal.ZERO);
            if (KPIStatus.COMPLETED.equals(a.getStatus())) {
                a.setStatus(KPIStatus.ACTIVE);
            }
        }
        assignmentRepository.saveAll(allAssignments);
        log.info("Reset {} assignments for {}/{}", allAssignments.size(), month, year);

        // Step 3: Apply accumulated counts to each assignment
        int kpiRecordsUpdated = 0;
        for (Map.Entry<Long, Map<String, Long>> empEntry : employeeKpiCounts.entrySet()) {
            Long spId = empEntry.getKey();
            for (Map.Entry<String, Long> kpiEntry : empEntry.getValue().entrySet()) {
                String kpiName = kpiEntry.getKey();
                BigDecimal total = BigDecimal.valueOf(kpiEntry.getValue());

                List<EmployeeKpiAssignment> matching =
                        assignmentRepository.findByEmployeeIdAndKpiNameAndMonthAndYear(spId, kpiName, month, year);
                for (EmployeeKpiAssignment a : matching) {
                    a.updateAchievedValue(total);
                    assignmentRepository.save(a);
                    kpiRecordsUpdated++;
                }
                if (matching.isEmpty()) {
                    log.warn("Backfill: no assignment for employeeId={} kpiName='{}' {}/{}", spId, kpiName, month, year);
                }
            }
        }

        log.info("Recalculate complete: {}/{} — reset={} processed={} skipped={} updated={}",
                month, year, allAssignments.size(), processed, skipped, kpiRecordsUpdated);

        return Map.of(
                "month", month,
                "year", year,
                "assignmentsReset", allAssignments.size(),
                "salesKpiUpdatesProcessed", processed,
                "salesKpiUpdatesSkipped", skipped,
                "kpiRecordsUpdated", kpiRecordsUpdated
        );
    }
}
