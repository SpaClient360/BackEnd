package com.htttql.crmmodule.billing.dto;

import com.htttql.crmmodule.common.enums.InvoiceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceStatusRequest {

    @NotNull(message = "Status is required")
    private InvoiceStatus status;

    private String notes;
}
