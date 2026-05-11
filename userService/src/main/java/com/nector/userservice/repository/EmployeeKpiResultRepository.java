package com.nector.userservice.repository;

import com.nector.userservice.enums.KPIGrade;
import com.nector.userservice.model.EmployeeKpiResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeKpiResultRepository extends JpaRepository<EmployeeKpiResult, Long> {

    List<EmployeeKpiResult> findByEmployeeId(Long employeeId);

    Optional<EmployeeKpiResult> findByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);

    List<EmployeeKpiResult> findByEmployeeIdAndYear(Long employeeId, Integer year);

    List<EmployeeKpiResult> findByMonthAndYear(Integer month, Integer year);

    List<EmployeeKpiResult> findByYear(Integer year);

    List<EmployeeKpiResult> findByFinalGrade(KPIGrade grade);

    Page<EmployeeKpiResult> findByEmployeeId(Long employeeId, Pageable pageable);

    Page<EmployeeKpiResult> findByMonthAndYear(Integer month, Integer year, Pageable pageable);

    Page<EmployeeKpiResult> findByYear(Integer year, Pageable pageable);

    @Query("SELECT ekr FROM EmployeeKpiResult ekr WHERE ekr.employeeId = :employeeId AND ekr.year = :year " +
           "ORDER BY ekr.month DESC")
    List<EmployeeKpiResult> findByEmployeeIdAndYearOrderByMonthDesc(@Param("employeeId") Long employeeId,
                                                                     @Param("year") Integer year);

    @Query("SELECT AVG(ekr.totalScore) FROM EmployeeKpiResult ekr WHERE ekr.employeeId = :employeeId AND ekr.year = :year")
    Double calculateAverageScoreByEmployeeAndYear(@Param("employeeId") Long employeeId,
                                                   @Param("year") Integer year);

    @Query("SELECT ekr.finalGrade, COUNT(ekr) FROM EmployeeKpiResult ekr " +
           "WHERE ekr.year = :year GROUP BY ekr.finalGrade")
    List<Object[]> countByGradeForYear(@Param("year") Integer year);

    @Query("SELECT ekr.finalGrade, COUNT(ekr) FROM EmployeeKpiResult ekr " +
           "WHERE ekr.month = :month AND ekr.year = :year GROUP BY ekr.finalGrade")
    List<Object[]> countByGradeForMonthAndYear(@Param("month") Integer month, @Param("year") Integer year);

    @Query("SELECT DISTINCT ekr.employeeId FROM EmployeeKpiResult ekr WHERE ekr.year = :year")
    List<Long> findDistinctEmployeeIdsByYear(@Param("year") Integer year);

    @Query("SELECT ekr FROM EmployeeKpiResult ekr " +
           "WHERE (:employeeId IS NULL OR ekr.employeeId = :employeeId) " +
           "AND (:month IS NULL OR ekr.month = :month) " +
           "AND (:year IS NULL OR ekr.year = :year) " +
           "AND (:grade IS NULL OR ekr.finalGrade = :grade) " +
           "ORDER BY ekr.year DESC, ekr.month DESC, ekr.totalScore DESC")
    Page<EmployeeKpiResult> findByFilters(@Param("employeeId") Long employeeId,
                                           @Param("month") Integer month,
                                           @Param("year") Integer year,
                                           @Param("grade") KPIGrade grade,
                                           Pageable pageable);

    boolean existsByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);

    @Query("SELECT ekr FROM EmployeeKpiResult ekr WHERE ekr.year = :year AND ekr.month = :month " +
           "ORDER BY ekr.totalScore DESC")
    List<EmployeeKpiResult> findTopPerformersByMonthAndYear(@Param("month") Integer month,
                                                              @Param("year") Integer year,
                                                              Pageable pageable);

    @Query("SELECT ekr FROM EmployeeKpiResult ekr WHERE ekr.year = :year " +
           "ORDER BY ekr.totalScore DESC")
    List<EmployeeKpiResult> findTopPerformersByYear(@Param("year") Integer year, Pageable pageable);
}
