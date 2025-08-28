package com.htttql.crmmodule.core.controller;

import com.htttql.crmmodule.core.dto.RoleRequest;
import com.htttql.crmmodule.core.dto.RoleResponse;
import com.htttql.crmmodule.core.service.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Role Management Controller
 * Manages system roles and permissions
 */
@Tag(name = "Role Management", description = "System roles and permissions management")
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService roleService;

    @Operation(summary = "Get all roles with pagination")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<Page<RoleResponse>> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<RoleResponse> roles = roleService.getAllRoles(page, size);
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "Get role by ID")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getRoleById(@PathVariable Long id) {
        RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "Create new role")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
        RoleResponse role = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    @Operation(summary = "Update role")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        RoleResponse role = roleService.updateRole(id, request);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "Delete role")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
