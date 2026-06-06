package com.nector.userservice.repository;

import com.nector.userservice.model.SalesKpiUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface SalesKpiUpdateRepository extends JpaRepository<SalesKpiUpdate, Long> {

    boolean existsByEmpCodeAndDate(String empCode, LocalDate date);
}
