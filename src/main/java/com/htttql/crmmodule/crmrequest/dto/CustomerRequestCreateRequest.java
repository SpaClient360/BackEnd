package com.htttql.crmmodule.crmrequest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequestCreateRequest {

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 32)
    private String phoneNumber;

    private String customerNote;

    @Size(max = 64)
    private String source;

    @Size(max = 45)
    private String ipAddress;
}
