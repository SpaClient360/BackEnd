package com.htttql.crmmodule.crmrequest.web;

import com.htttql.crmmodule.crmrequest.domain.CustomerRequestStatus;
import com.htttql.crmmodule.crmrequest.dto.CustomerRequestCreateRequest;
import com.htttql.crmmodule.crmrequest.dto.CustomerRequestResponse;
import com.htttql.crmmodule.crmrequest.service.CustomerRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer-requests")
@Tag(name = "Customer Requests", description = "API for managing customer requests")
public class CustomerRequestController {

    private final CustomerRequestService service;

    public CustomerRequestController(CustomerRequestService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create customer request", description = "Create a new customer request with anti-spam and rate limiting")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer request created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Duplicate request detected"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    @CrossOrigin(origins = "*")
    public ResponseEntity<CustomerRequestResponse> create(
            HttpServletRequest httpRequest,
            @RequestBody @Valid CustomerRequestCreateRequest request) {

        String ip = extractClientIp(httpRequest);
        CustomerRequestResponse response = service.create(ip, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer request by ID", description = "Retrieve a specific customer request by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer request found"),
            @ApiResponse(responseCode = "404", description = "Customer request not found")
    })
    @CrossOrigin(origins = "*")
    public ResponseEntity<CustomerRequestResponse> getOne(
            @Parameter(description = "Customer request UUID") @PathVariable("id") UUID id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    @GetMapping
    @Operation(summary = "Get customer requests page", description = "Retrieve a paginated list of customer requests")
    @ApiResponse(responseCode = "200", description = "Page of customer requests")
    @CrossOrigin(origins = "*")
    public ResponseEntity<Page<CustomerRequestResponse>> getPage(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.getPage(pageable));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update customer request status", description = "Update the status of a customer request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Customer request not found")
    })
    @CrossOrigin(origins = "*")
    public ResponseEntity<CustomerRequestResponse> updateStatus(
            @Parameter(description = "Customer request UUID") @PathVariable UUID id,
            @RequestBody Map<String, String> statusUpdate) {

        CustomerRequestStatus newStatus = CustomerRequestStatus.valueOf(statusUpdate.get("status"));
        CustomerRequestResponse updated = service.updateStatus(id, newStatus);
        return ResponseEntity.ok(updated);
    }

    private String extractClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
