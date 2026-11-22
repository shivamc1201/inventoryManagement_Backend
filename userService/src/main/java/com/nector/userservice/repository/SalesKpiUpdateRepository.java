package com.nector.userservice.repository;

import com.nector.userservice.model.SalesKpiUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalesKpiUpdateRepository extends JpaRepository<SalesKpiUpdate, Long> {

    boolean existsByEmpCodeAndDate(String empCode, LocalDate date);

    @Query("SELECT s FROM SalesKpiUpdate s LEFT JOIN FETCH s.meetingDetails WHERE s.date BETWEEN :dateFrom AND :dateTo ORDER BY s.id ASC")
    List<SalesKpiUpdate> findByDateBetweenWithDetails(@Param("dateFrom") LocalDate dateFrom,
                                                      @Param("dateTo") LocalDate dateTo);

    // Returns IDs of all rows that are not the canonical (lowest-id) row for their empCode+date pair.
    @Query(value = """
            SELECT id FROM sales_KPI_update
            WHERE date BETWEEN :dateFrom AND :dateTo
              AND id NOT IN (
                  SELECT MIN(id) FROM sales_KPI_update
                  WHERE date BETWEEN :dateFrom AND :dateTo
                  GROUP BY emp_code, date
              )
            """, nativeQuery = true)
    List<Long> findDuplicateIds(@Param("dateFrom") LocalDate dateFrom,
                                @Param("dateTo") LocalDate dateTo);

    @Modifying
    @Query(value = "DELETE FROM sales_kpi_meeting_details WHERE sales_kpi_update_id IN :ids", nativeQuery = true)
    void deleteMeetingDetailsByUpdateIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query(value = "DELETE FROM sales_KPI_update WHERE id IN :ids", nativeQuery = true)
    void deleteByIds(@Param("ids") List<Long> ids);
}
