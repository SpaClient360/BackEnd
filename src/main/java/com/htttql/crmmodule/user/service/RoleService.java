package com.htttql.crmmodule.user.service;

import com.htttql.crmmodule.user.dto.RoleDTO;
import com.htttql.crmmodule.user.entity.Role;
import com.htttql.crmmodule.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Role operations
 */
@Service
@Transactional
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    /**
     * Create a new role
     */
    public RoleDTO createRole(RoleDTO roleDTO) {
        // Check if role name already exists
        if (roleRepository.existsByRoleName(roleDTO.getRoleName())) {
            throw new RuntimeException("Role name already exists: " + roleDTO.getRoleName());
        }

        Role role = new Role();
        role.setRoleName(roleDTO.getRoleName());
        role.setDescription(roleDTO.getDescription());

        Role savedRole = roleRepository.save(role);
        return convertToDTO(savedRole);
    }

    /**
     * Get role by id
     */
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Long id) {
        return roleRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
    }

    /**
     * Get role by name
     */
    @Transactional(readOnly = true)
    public RoleDTO getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Role not found with name: " + roleName));
    }

    /**
     * Update role
     */
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        // Check role name uniqueness if changed
        if (!role.getRoleName().equals(roleDTO.getRoleName()) &&
                roleRepository.existsByRoleName(roleDTO.getRoleName())) {
            throw new RuntimeException("Role name already exists: " + roleDTO.getRoleName());
        }

        role.setRoleName(roleDTO.getRoleName());
        role.setDescription(roleDTO.getDescription());

        Role updatedRole = roleRepository.save(role);
        return convertToDTO(updatedRole);
    }

    /**
     * Delete role
     */
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        // Check if role has users
        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
            throw new RuntimeException("Cannot delete role. Role is assigned to users.");
        }

        roleRepository.deleteById(id);
    }

    /**
     * Get all roles with pagination
     */
    @Transactional(readOnly = true)
    public Page<RoleDTO> getAllRoles(Pageable pageable) {
        return roleRepository.findAllByOrderByRoleNameAsc(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Search roles by keyword
     */
    @Transactional(readOnly = true)
    public Page<RoleDTO> searchRoles(String keyword, Pageable pageable) {
        return roleRepository.searchByKeyword(keyword, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get all roles as list
     */
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRolesAsList() {
        return roleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Initialize default roles
     */
    @Transactional
    public void initializeDefaultRoles() {
        if (roleRepository.count() == 0) {
            // Create default roles
            Role adminRole = new Role();
            adminRole.setRoleName("ADMIN");
            adminRole.setDescription("Administrator with full access");
            roleRepository.save(adminRole);

            Role managerRole = new Role();
            managerRole.setRoleName("MANAGER");
            managerRole.setDescription("Manager with limited administrative access");
            roleRepository.save(managerRole);

            Role staffRole = new Role();
            staffRole.setRoleName("STAFF");
            staffRole.setDescription("Staff member with basic access");
            roleRepository.save(staffRole);

        }
    }

    /**
     * Convert Role entity to RoleDTO
     */
    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = RoleDTO.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();

        // Add user count if available
        if (role.getUsers() != null) {
            dto.setUserCount((long) role.getUsers().size());
        }

        return dto;
    }
}