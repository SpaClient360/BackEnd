package com.htttql.crmmodule.service.service;

import com.htttql.crmmodule.service.dto.*;

import java.util.List;

public interface IDashboardService {
    ReceptionistDashboardStats getReceptionistDashboardStats();

    List<ChartDataPoint> getAppointmentStatusChart();

    List<ChartDataPoint> getAppointmentTrendChart();

    List<ChartDataPoint> getServicePopularityChart();

    List<ChartDataPoint> getCustomerTiersChart();

    List<ChartDataPoint> getRevenueTrendChart();

    MonthlyPerformance getMonthlyPerformance();
}
