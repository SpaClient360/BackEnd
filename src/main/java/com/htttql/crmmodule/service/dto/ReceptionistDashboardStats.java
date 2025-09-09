package com.htttql.crmmodule.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionistDashboardStats {
    // Today's statistics
    private int todayAppointments;
    private int todayCheckIns;
    private int todayCompleted;
    private int todayNoShows;
    private int pendingRequests;
    private int newCustomersToday;

    // Weekly statistics
    private int weekAppointments;
    private int weekRevenue;
    private int weekNewCustomers;

    // Monthly statistics
    private int monthAppointments;
    private int monthRevenue;
    private int monthNewCustomers;

    // Quick stats
    private BigDecimal averageAppointmentValue;
    private double customerRetentionRate;
    private int activeCustomers;
    private int totalCustomers;

    // Status breakdown
    private int scheduledAppointments;
    private int confirmedAppointments;
    private int inProgressAppointments;
    private int completedAppointments;
    private int cancelledAppointments;
}
