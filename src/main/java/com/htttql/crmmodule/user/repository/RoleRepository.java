package com.htttql.crmmodule.user.repository;

import com.htttql.crmmodule.user.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Role entity
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find role by role name
     */
    Optional<Role> findByRoleName(String roleName);

    /**
     * Check if role name exists
     */
    boolean existsByRoleName(String roleName);

    /**
     * Find roles by role name containing (case insensitive)
     */
    List<Role> findByRoleNameContainingIgnoreCase(String roleName);

    /**
     * Search roles by role name or description
     */
    @Query("SELECT r FROM Role r WHERE LOWER(r.roleName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Role> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find all roles with pagination
     */
    Page<Role> findAllByOrderByRoleNameAsc(Pageable pageable);
}