package com.htttql.crmmodule.core.dto;

import com.htttql.crmmodule.common.enums.StaffStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffUserStatusRequest {

    @NotNull(message = "Status is required")
    private StaffStatus status;
}
