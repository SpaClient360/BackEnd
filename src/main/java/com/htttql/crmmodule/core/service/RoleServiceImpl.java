package com.htttql.crmmodule.core.service;

import com.htttql.crmmodule.core.dto.RoleRequest;
import com.htttql.crmmodule.core.dto.RoleResponse;
import com.htttql.crmmodule.core.entity.Role;
import com.htttql.crmmodule.core.repository.IRoleRepository;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements IRoleService {

    private final IRoleRepository roleRepository;

    @Override
    public Page<RoleResponse> getAllRoles(int page, int size) {
        Page<Role> roles = roleRepository.findAll(PageRequest.of(page, size));
        return roles.map(this::mapToResponse);
    }

    @Override
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return mapToResponse(role);
    }

    @Override
    public RoleResponse createRole(RoleRequest request) {
        Role role = Role.builder()
                .code(request.getName().toUpperCase())
                .name(request.getName())
                .description(request.getDescription())
                .build();

        Role savedRole = roleRepository.save(role);
        return mapToResponse(savedRole);
    }

    @Override
    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        role.setCode(request.getName().toUpperCase());
        role.setName(request.getName());
        role.setDescription(request.getDescription());

        Role updatedRole = roleRepository.save(role);
        return mapToResponse(updatedRole);
    }

    @Override
    public void deleteRole(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }

    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .roleId(role.getRoleId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getCode())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}
