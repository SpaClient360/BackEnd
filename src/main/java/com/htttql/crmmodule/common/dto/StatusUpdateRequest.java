package com.htttql.crmmodule.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic status update request DTO
 * Used for updating status of entities via REST API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Status update request")
public class StatusUpdateRequest {

    @Schema(description = "New status value", example = "ACTIVE", required = true)
    @NotBlank(message = "Status is required")
    private String status;

    @Schema(description = "Optional reason for status change", example = "Customer requested cancellation")
    private String reason;

    @Schema(description = "Additional notes", example = "Status updated by admin")
    private String notes;
}
