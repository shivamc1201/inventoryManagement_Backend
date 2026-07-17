package com.nector.userservice.repository;

import com.nector.userservice.model.SalesPerson;
import com.nector.userservice.enums.SalesRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesPersonRepository extends JpaRepository<SalesPerson, Long> {

    Optional<SalesPerson> findByEmployeeRollNo(String employeeRollNo);

    boolean existsByEmployeeRollNo(String employeeRollNo);

    List<SalesPerson> findByRole(SalesRole role);

    List<SalesPerson> findByZone(String zone);

    List<SalesPerson> findByManagerId(Long managerId);

    List<SalesPerson> findByActiveTrue();

    @Query("SELECT sp FROM SalesPerson sp WHERE sp.active = true AND " +
           "(LOWER(sp.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sp.employeeRollNo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(sp.zone) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<SalesPerson> searchActiveSalesPersons(@Param("search") String search);

    @Query("SELECT sp FROM SalesPerson sp WHERE sp.active = true AND sp.role = :role")
    List<SalesPerson> findActiveByRole(@Param("role") SalesRole role);

    @Query("SELECT COUNT(sp) FROM SalesPerson sp WHERE sp.managerId = :managerId AND sp.active = true")
    long countActiveSubordinates(@Param("managerId") Long managerId);

    @Query("SELECT sp FROM SalesPerson sp WHERE sp.active = true AND sp.role IN :roles")
    List<SalesPerson> findActiveByRoles(@Param("roles") List<SalesRole> roles);

    @Query("SELECT sp FROM SalesPerson sp WHERE sp.active = true AND sp.managerId IS NULL")
    List<SalesPerson> findActiveTopLevelManagers();

    @Query("SELECT sp FROM SalesPerson sp WHERE sp.active = true AND sp.managerId = :managerId")
    List<SalesPerson> findActiveByManagerId(@Param("managerId") Long managerId);

    boolean existsByPhone(String phone);

    Optional<SalesPerson> findByPhone(String phone);

    boolean existsByEmail(String email);

    Optional<SalesPerson> findByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, long l);
    Optional<SalesPerson> findByUsername(String username);

    @Query("SELECT COUNT(sp) FROM SalesPerson sp WHERE sp.active = true")
    long countActiveSalesPersons();

    Optional<SalesPerson> findFirstByRoleAndActiveTrueOrderByIdAsc(SalesRole role);
}
