package com.nector.userservice.repository;

import com.nector.userservice.common.RoleType;
import com.nector.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
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
    List<User> findByRoleType(RoleType roleType);
    List<User> findByRoleTypeIn(List<RoleType> roleTypes);
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.username = :username")
    Optional<User> findByUsernameWithRolesAndPermissions(@Param("username") String username);
    
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.id = :id")
    Optional<User> findByIdWithRolesAndPermissions(@Param("id") Long id);
}