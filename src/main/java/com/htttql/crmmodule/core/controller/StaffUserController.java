package com.htttql.crmmodule.core.controller;

import com.htttql.crmmodule.core.dto.StaffUserRequest;
import com.htttql.crmmodule.core.dto.StaffUserResponse;
import com.htttql.crmmodule.core.dto.StaffUserStatusRequest;
import com.htttql.crmmodule.core.service.IStaffUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Staff User Management", description = "Staff User CRUD operations")
@RestController
@RequestMapping("/api/staff-users")
@RequiredArgsConstructor
public class StaffUserController {

    private final IStaffUserService staffUserService;

    @Operation(summary = "Get all staff users with pagination")
    @GetMapping
    public ResponseEntity<Page<StaffUserResponse>> getAllStaffUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "staffId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<StaffUserResponse> staffUsers = staffUserService.getAllStaffUsers(pageable);
        return ResponseEntity.ok(staffUsers);
    }

    @Operation(summary = "Get staff user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<StaffUserResponse> getStaffUserById(@PathVariable Long id) {
        StaffUserResponse staffUser = staffUserService.getStaffUserById(id);
        return ResponseEntity.ok(staffUser);
    }

    @Operation(summary = "Create new staff user")
    @PostMapping
    public ResponseEntity<StaffUserResponse> createStaffUser(@Valid @RequestBody StaffUserRequest request) {
        StaffUserResponse staffUser = staffUserService.createStaffUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(staffUser);
    }

    @Operation(summary = "Update staff user")
    @PutMapping("/{id}")
    public ResponseEntity<StaffUserResponse> updateStaffUser(
            @PathVariable Long id,
            @Valid @RequestBody StaffUserRequest request) {
        StaffUserResponse staffUser = staffUserService.updateStaffUser(id, request);
        return ResponseEntity.ok(staffUser);
    }

    @Operation(summary = "Delete staff user")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStaffUser(@PathVariable Long id) {
        staffUserService.deleteStaffUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update staff user status")
    @PutMapping("/{id}/status")
    public ResponseEntity<StaffUserResponse> updateStaffUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody StaffUserStatusRequest request) {
        StaffUserResponse staffUser = staffUserService.updateStaffUserStatus(id, request);
        return ResponseEntity.ok(staffUser);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

}
