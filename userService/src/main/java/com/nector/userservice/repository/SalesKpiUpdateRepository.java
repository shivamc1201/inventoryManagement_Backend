package com.nector.userservice.repository;

import com.nector.userservice.model.SalesKpiUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalesKpiUpdateRepository extends JpaRepository<SalesKpiUpdate, Long> {

    boolean existsByEmpCodeAndDate(String empCode, LocalDate date);

    @Query("SELECT s FROM SalesKpiUpdate s LEFT JOIN FETCH s.meetingDetails WHERE s.date BETWEEN :dateFrom AND :dateTo")
    List<SalesKpiUpdate> findByDateBetweenWithDetails(@Param("dateFrom") LocalDate dateFrom,
                                                       @Param("dateTo") LocalDate dateTo);
}
