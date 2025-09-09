package com.htttql.crmmodule.service.controller;

import com.htttql.crmmodule.service.dto.*;
import com.htttql.crmmodule.service.service.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Dashboard Analytics", description = "Dashboard statistics and analytics endpoints")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    @Operation(summary = "Get receptionist dashboard statistics")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @GetMapping("/receptionist/stats")
    public ResponseEntity<ReceptionistDashboardStats> getReceptionistStats() {
        ReceptionistDashboardStats stats = dashboardService.getReceptionistDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get appointment status distribution for charts")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @GetMapping("/charts/appointment-status")
    public ResponseEntity<List<ChartDataPoint>> getAppointmentStatusChart() {
        List<ChartDataPoint> data = dashboardService.getAppointmentStatusChart();
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "Get daily appointment trend for last 7 days")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @GetMapping("/charts/appointment-trend")
    public ResponseEntity<List<ChartDataPoint>> getAppointmentTrendChart() {
        List<ChartDataPoint> data = dashboardService.getAppointmentTrendChart();
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "Get service popularity chart")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @GetMapping("/charts/service-popularity")
    public ResponseEntity<List<ChartDataPoint>> getServicePopularityChart() {
        List<ChartDataPoint> data = dashboardService.getServicePopularityChart();
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "Get customer tier distribution")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @GetMapping("/charts/customer-tiers")
    public ResponseEntity<List<ChartDataPoint>> getCustomerTiersChart() {
        List<ChartDataPoint> data = dashboardService.getCustomerTiersChart();
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "Get revenue trend for last 30 days")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @GetMapping("/charts/revenue-trend")
    public ResponseEntity<List<ChartDataPoint>> getRevenueTrendChart() {
        List<ChartDataPoint> data = dashboardService.getRevenueTrendChart();
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "Get monthly performance summary")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @GetMapping("/performance/monthly")
    public ResponseEntity<MonthlyPerformance> getMonthlyPerformance() {
        MonthlyPerformance performance = dashboardService.getMonthlyPerformance();
        return ResponseEntity.ok(performance);
    }
}
