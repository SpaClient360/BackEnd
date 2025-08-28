package com.htttql.crmmodule.service.controller;

import com.htttql.crmmodule.service.dto.ServiceRequest;
import com.htttql.crmmodule.service.dto.ServiceResponse;
import com.htttql.crmmodule.service.service.IServiceService;
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

@Tag(name = "Service Management", description = "Service CRUD operations")
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final IServiceService serviceService;

    @Operation(summary = "Get all services with pagination")
    @GetMapping
    public ResponseEntity<Page<ServiceResponse>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "serviceId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ServiceResponse> services = serviceService.getAllServices(pageable);
        return ResponseEntity.ok(services);
    }

    @Operation(summary = "Get service by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable Long id) {
        ServiceResponse service = serviceService.getServiceById(id);
        return ResponseEntity.ok(service);
    }

    @Operation(summary = "Create new service")
    @PostMapping
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody ServiceRequest request) {
        ServiceResponse service = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(service);
    }

    @Operation(summary = "Update service")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceRequest request) {
        ServiceResponse service = serviceService.updateService(id, request);
        return ResponseEntity.ok(service);
    }

    @Operation(summary = "Delete service")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
