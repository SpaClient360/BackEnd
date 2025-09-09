package com.htttql.crmmodule.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPerformance {
    private LocalDate month;
    private int totalAppointments;
    private int completedAppointments;
    private int cancelledAppointments;
    private BigDecimal totalRevenue;
    private BigDecimal averageRevenuePerAppointment;
    private int newCustomers;
    private int returningCustomers;
    private BigDecimal customerAcquisitionCost;
    private double customerSatisfactionScore;
    private int staffUtilizationRate;
}
