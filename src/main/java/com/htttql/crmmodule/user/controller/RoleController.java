package com.htttql.crmmodule.user.controller;

import com.htttql.crmmodule.user.dto.RoleDTO;
import com.htttql.crmmodule.user.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Role operations
 */
@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * Create new role
     */
    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        try {
            RoleDTO createdRole = roleService.createRole(roleDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get role by id
     */
    @GetMapping("/{id}")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable Long id) {
        try {
            RoleDTO role = roleService.getRoleById(id);
            return ResponseEntity.ok(role);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get role by name
     */
    @GetMapping("/name/{roleName}")
    public ResponseEntity<RoleDTO> getRoleByName(@PathVariable String roleName) {
        try {
            RoleDTO role = roleService.getRoleByName(roleName);
            return ResponseEntity.ok(role);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update role
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable Long id,
            @Valid @RequestBody RoleDTO roleDTO) {
        try {
            RoleDTO updatedRole = roleService.updateRole(id, roleDTO);
            return ResponseEntity.ok(updatedRole);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete role
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        try {
            roleService.deleteRole(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all roles with pagination
     */
    @GetMapping
    public ResponseEntity<Page<RoleDTO>> getAllRoles(@PageableDefault(size = 10) Pageable pageable) {
        Page<RoleDTO> roles = roleService.getAllRoles(pageable);
        return ResponseEntity.ok(roles);
    }

    /**
     * Search roles by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<Page<RoleDTO>> searchRoles(@RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<RoleDTO> roles = roleService.searchRoles(keyword, pageable);
        return ResponseEntity.ok(roles);
    }

    /**
     * Get all roles as list (for dropdowns)
     */
    @GetMapping("/list")
    public ResponseEntity<List<RoleDTO>> getAllRolesAsList() {
        List<RoleDTO> roles = roleService.getAllRolesAsList();
        return ResponseEntity.ok(roles);
    }

    /**
     * Initialize default roles
     */
    @PostMapping("/initialize")
    public ResponseEntity<String> initializeDefaultRoles() {
        roleService.initializeDefaultRoles();
        return ResponseEntity.ok("Default roles initialized successfully");
    }

    /**
     * Exception handler for validation errors
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        ErrorResponse error = new ErrorResponse("ERROR", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Error response class
     */
    public static class ErrorResponse {
        private String status;
        private String message;

        public ErrorResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        // Getters and setters
        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}