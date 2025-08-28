package com.htttql.crmmodule.lead.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 200, message = "Full name must be between 2 and 200 characters")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone must be 10-11 digits")
    private String phone;

    private String note;
}
