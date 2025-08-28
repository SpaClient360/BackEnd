package com.htttql.crmmodule.core.service;

import com.htttql.crmmodule.core.dto.RoleRequest;
import com.htttql.crmmodule.core.dto.RoleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Role operations
 */
public interface IRoleService {

    /**
     * Get all roles with pagination
     */
    Page<RoleResponse> getAllRoles(int page, int size);

    /**
     * Get role by ID
     */
    RoleResponse getRoleById(Long id);

    /**
     * Create new role
     */
    RoleResponse createRole(RoleRequest request);

    /**
     * Update role
     */
    RoleResponse updateRole(Long id, RoleRequest request);

    /**
     * Delete role
     */
    void deleteRole(Long id);
}
