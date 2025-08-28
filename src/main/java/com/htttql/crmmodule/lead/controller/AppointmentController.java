package com.htttql.crmmodule.lead.controller;

import com.htttql.crmmodule.lead.dto.AppointmentRequest;
import com.htttql.crmmodule.lead.dto.AppointmentResponse;
import com.htttql.crmmodule.lead.service.IAppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Appointment Management Controller
 * Manages customer appointments and scheduling
 */
@Tag(name = "Appointment Management", description = "Customer appointments and scheduling management")
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final IAppointmentService appointmentService;

    @Operation(summary = "Get all appointments with pagination")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    @GetMapping
    public ResponseEntity<Page<AppointmentResponse>> getAllAppointments(Pageable pageable) {
        Page<AppointmentResponse> appointments = appointmentService.getAllAppointments(pageable);
        return ResponseEntity.ok(appointments);
    }

    @Operation(summary = "Get appointment by ID")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
        AppointmentResponse appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointment);
    }

    @Operation(summary = "Create new appointment")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse appointment = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointment);
    }

    @Operation(summary = "Update appointment")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse appointment = appointmentService.updateAppointment(id, request);
        return ResponseEntity.ok(appointment);
    }

    @Operation(summary = "Delete appointment")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update appointment status")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        AppointmentResponse appointment = appointmentService.updateAppointmentStatus(id, status);
        return ResponseEntity.ok(appointment);
    }
}
