package com.nector.userservice.repository;

import com.nector.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndPassword(String username, String password);

    Optional<User> findByUsername(String username);

    List<User> findByRoleType(String roleType);

    List<User> findByRoleTypeIn(List<String> roleTypes);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles r WHERE u.username = :username")
    Optional<User> findByUsernameWithRolesAndPermissions(@Param("username") String username);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles r WHERE u.id = :id")
    Optional<User> findByIdWithRolesAndPermissions(@Param("id") Long id);

    @Modifying
    @Query("DELETE FROM UserApproval ua WHERE ua.user.id = :userId")
    void deleteUserApprovalsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(u) FROM User u")
    long countAllUsers();

    Optional<User> findByEmployeeRollNo(String employeeRollNo);
}
