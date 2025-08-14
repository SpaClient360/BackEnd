package com.htttql.crmmodule.crmrequest.dto;

import com.htttql.crmmodule.crmrequest.domain.CustomerRequestStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CustomerRequestResponse {
    private UUID id;
    private String name;
    private String phoneNumber;
    private String customerNote;
    private String source;
    private String ipAddress;
    private CustomerRequestStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
