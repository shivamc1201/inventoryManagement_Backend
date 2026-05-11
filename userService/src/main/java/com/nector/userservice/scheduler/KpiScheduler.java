package com.nector.userservice.scheduler;

import com.nector.userservice.service.EmployeeKpiAssignmentService;
import com.nector.userservice.service.EmployeeKpiResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class KpiScheduler {

    private final EmployeeKpiAssignmentService assignmentService;
    private final EmployeeKpiResultService resultService;

    /**
     * Run daily at midnight to expire old assignments
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void expireOldAssignments() {
        log.info("Running scheduled task: expireOldAssignments at {}", LocalDate.now());
        try {
            assignmentService.expireOldAssignments();
            log.info("Completed scheduled task: expireOldAssignments");
        } catch (Exception e) {
            log.error("Error in expireOldAssignments scheduled task", e);
        }
    }

    /**
     * Run on the 1st of every month at 1 AM to generate results for previous month
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void generateMonthlyResults() {
        log.info("Running scheduled task: generateMonthlyResults");
        try {
            LocalDate previousMonth = LocalDate.now().minusMonths(1);
            resultService.generateResultsForAllEmployees(previousMonth.getMonthValue(), previousMonth.getYear());
            log.info("Completed scheduled task: generateMonthlyResults for {}/{}", 
                    previousMonth.getMonthValue(), previousMonth.getYear());
        } catch (Exception e) {
            log.error("Error in generateMonthlyResults scheduled task", e);
        }
    }
}
