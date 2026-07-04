package com.nector.userservice.repository;

import com.nector.userservice.enums.KPIStatus;
import com.nector.userservice.model.EmployeeKpiAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeKpiAssignmentRepository extends JpaRepository<EmployeeKpiAssignment, Long> {

    List<EmployeeKpiAssignment> findByEmployeeId(Long employeeId);

    List<EmployeeKpiAssignment> findByEmployeeIdAndStatus(Long employeeId, KPIStatus status);

    Page<EmployeeKpiAssignment> findByEmployeeId(Long employeeId, Pageable pageable);

    List<EmployeeKpiAssignment> findByKpiId(Long kpiId);

    List<EmployeeKpiAssignment> findByStatus(KPIStatus status);

    @Query("SELECT eka FROM EmployeeKpiAssignment eka WHERE eka.employeeId = :employeeId " +
           "AND eka.status = :status AND eka.startDate <= :date AND eka.endDate >= :date")
    List<EmployeeKpiAssignment> findActiveByEmployeeIdAndDate(@Param("employeeId") Long employeeId,
                                                               @Param("status") KPIStatus status,
                                                               @Param("date") LocalDate date);

    @Query("SELECT eka FROM EmployeeKpiAssignment eka WHERE eka.employeeId = :employeeId " +
           "AND eka.kpiId = :kpiId AND eka.status = :status")
    Optional<EmployeeKpiAssignment> findByEmployeeIdAndKpiIdAndStatus(@Param("employeeId") Long employeeId,
                                                                       @Param("kpiId") Long kpiId,
                                                                       @Param("status") KPIStatus status);

    @Query("SELECT SUM(eka.weightage) FROM EmployeeKpiAssignment eka " +
           "WHERE eka.employeeId = :employeeId AND eka.status = :status")
    Integer sumWeightageByEmployeeIdAndStatus(@Param("employeeId") Long employeeId,
                                              @Param("status") KPIStatus status);

    @Query("SELECT SUM(eka.weightage) FROM EmployeeKpiAssignment eka " +
           "WHERE eka.employeeId = :employeeId AND eka.status = :status " +
           "AND eka.assignedMonth = :month AND eka.assignedYear = :year")
    Integer sumWeightageByEmployeeIdAndStatusAndMonth(@Param("employeeId") Long employeeId,
                                                      @Param("status") KPIStatus status,
                                                      @Param("month") int month,
                                                      @Param("year") int year);

    /**
     * Find assignments that OVERLAP a given date range (used for monthly result generation).
     * An assignment overlaps the month if it starts before month end AND ends after month start.
     */
    @Query("SELECT eka FROM EmployeeKpiAssignment eka " +
           "WHERE eka.employeeId = :employeeId " +
           "AND eka.startDate <= :endDate AND eka.endDate >= :startDate")
    List<EmployeeKpiAssignment> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                              @Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);

    /**
     * Find assignments by employee, month and year (using the stored month/year fields).
     */
    @Query("SELECT eka FROM EmployeeKpiAssignment eka " +
           "WHERE eka.employeeId = :employeeId AND eka.assignedMonth = :month AND eka.assignedYear = :year")
    List<EmployeeKpiAssignment> findByEmployeeIdAndMonthAndYear(@Param("employeeId") Long employeeId,
                                                                 @Param("month") Integer month,
                                                                 @Param("year") Integer year);

    /**
     * Find all assignments for a given month and year (for admin monthly reports).
     */
    @Query("SELECT eka FROM EmployeeKpiAssignment eka " +
           "WHERE eka.assignedMonth = :month AND eka.assignedYear = :year")
    List<EmployeeKpiAssignment> findAllByMonthAndYear(@Param("month") Integer month,
                                                       @Param("year") Integer year);

    @Query("SELECT eka FROM EmployeeKpiAssignment eka " +
           "WHERE eka.startDate <= :endDate AND eka.endDate >= :startDate AND eka.status = :status")
    List<EmployeeKpiAssignment> findByDateRangeAndStatus(@Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate,
                                                           @Param("status") KPIStatus status);

    @Query("SELECT eka FROM EmployeeKpiAssignment eka " +
           "WHERE eka.employeeId = :employeeId " +
           "AND ((eka.startDate <= :endDate AND eka.endDate >= :startDate))")
    List<EmployeeKpiAssignment> findOverlappingAssignments(@Param("employeeId") Long employeeId,
                                                            @Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate);

    @Query("SELECT eka FROM EmployeeKpiAssignment eka " +
           "WHERE eka.employeeId = :employeeId AND eka.kpiId = :kpiId " +
           "AND eka.status = :status " +
           "AND ((eka.startDate <= :endDate AND eka.endDate >= :startDate))")
    Optional<EmployeeKpiAssignment> findOverlappingByEmployeeAndKpi(@Param("employeeId") Long employeeId,
                                                                       @Param("kpiId") Long kpiId,
                                                                       @Param("status") KPIStatus status,
                                                                       @Param("startDate") LocalDate startDate,
                                                                       @Param("endDate") LocalDate endDate);

    @Query("SELECT eka FROM EmployeeKpiAssignment eka " +
           "WHERE eka.employeeId = :employeeId AND eka.kpiId = :kpiId " +
           "AND eka.status = :status " +
           "AND eka.assignedMonth = :month AND eka.assignedYear = :year")
    Optional<EmployeeKpiAssignment> findByEmployeeIdAndKpiIdAndStatusAndMonth(@Param("employeeId") Long employeeId,
                                                                               @Param("kpiId") Long kpiId,
                                                                               @Param("status") KPIStatus status,
                                                                               @Param("month") int month,
                                                                               @Param("year") int year);

    @Query("SELECT eka FROM EmployeeKpiAssignment eka " +
           "WHERE eka.employeeId = :employeeId " +
           "AND eka.startDate <= CURRENT_DATE AND eka.endDate >= CURRENT_DATE " +
           "AND eka.status = 'ACTIVE'")
    List<EmployeeKpiAssignment> findCurrentActiveByEmployeeId(@Param("employeeId") Long employeeId);

    @Query("SELECT eka FROM EmployeeKpiAssignment eka " +
           "WHERE eka.status = 'ACTIVE' AND eka.endDate < CURRENT_DATE")
    List<EmployeeKpiAssignment> findExpiredAssignments();

    @Query("SELECT eka FROM EmployeeKpiAssignment eka " +
           "WHERE eka.employeeId = :employeeId AND eka.kpiId = :kpiId " +
           "AND eka.status != 'COMPLETED'")
    List<EmployeeKpiAssignment> findIncompleteByEmployeeAndKpi(@Param("employeeId") Long employeeId,
                                                                  @Param("kpiId") Long kpiId);

    Page<EmployeeKpiAssignment> findByStatus(KPIStatus status, Pageable pageable);

    @Query("SELECT eka FROM EmployeeKpiAssignment eka " +
           "WHERE (:employeeId IS NULL OR eka.employeeId = :employeeId) " +
           "AND (:kpiId IS NULL OR eka.kpiId = :kpiId) " +
           "AND (:status IS NULL OR eka.status = :status) " +
           "AND (:startDateFrom IS NULL OR eka.startDate >= :startDateFrom) " +
           "AND (:startDateTo IS NULL OR eka.startDate <= :startDateTo) " +
           "AND (:endDateFrom IS NULL OR eka.endDate >= :endDateFrom) " +
           "AND (:endDateTo IS NULL OR eka.endDate <= :endDateTo) " +
           "AND (:designation IS NULL OR LOWER(eka.designation) LIKE LOWER(CONCAT('%', :designation, '%'))) " +
           "AND (:roleName IS NULL OR LOWER(eka.roleName) LIKE LOWER(CONCAT('%', :roleName, '%')))" )
    Page<EmployeeKpiAssignment> findByFilters(@Param("employeeId") Long employeeId,
                                               @Param("kpiId") Long kpiId,
                                               @Param("status") KPIStatus status,
                                               @Param("startDateFrom") LocalDate startDateFrom,
                                               @Param("startDateTo") LocalDate startDateTo,
                                               @Param("endDateFrom") LocalDate endDateFrom,
                                               @Param("endDateTo") LocalDate endDateTo,
                                               @Param("designation") String designation,
                                               @Param("roleName") String roleName,
                                               Pageable pageable);

    long countByEmployeeIdAndStatus(Long employeeId, KPIStatus status);

    @Query("SELECT DISTINCT eka.employeeId FROM EmployeeKpiAssignment eka WHERE eka.status = :status")
    List<Long> findDistinctEmployeeIdsByStatus(@Param("status") KPIStatus status);

    /**
     * Find active KPI assignments for an employee by EXACT KPI name match.
     * Used for internal auto-update of: "Sale Target", "Dealer Onboard", "Distributor Onboard"
     */
    @Query("SELECT eka FROM EmployeeKpiAssignment eka JOIN eka.kpiMaster km " +
           "WHERE eka.employeeId = :employeeId " +
           "AND eka.status = 'ACTIVE' " +
           "AND eka.startDate <= CURRENT_DATE AND eka.endDate >= CURRENT_DATE " +
           "AND km.kpiName = :kpiName")
    List<EmployeeKpiAssignment> findActiveByEmployeeIdAndKpiName(
            @Param("employeeId") Long employeeId,
            @Param("kpiName") String kpiName);

    /**
     * Find assignments for a specific month/year by KPI name — used for backfill recalculation
     * where date-range constraint would exclude past-month assignments.
     */
    @Query("SELECT eka FROM EmployeeKpiAssignment eka JOIN eka.kpiMaster km " +
           "WHERE eka.employeeId = :employeeId " +
           "AND eka.assignedMonth = :month AND eka.assignedYear = :year " +
           "AND km.kpiName = :kpiName")
    List<EmployeeKpiAssignment> findByEmployeeIdAndKpiNameAndMonthAndYear(
            @Param("employeeId") Long employeeId,
            @Param("kpiName") String kpiName,
            @Param("month") int month,
            @Param("year") int year);
}
