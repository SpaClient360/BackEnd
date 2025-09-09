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
public class ChartDataPoint {
    private String label;
    private String category;
    private BigDecimal value;
    private Integer count;
    private String color;
    private String date;
    private String time;
}
