package com.htttql.crmmodule.crmrequest.mapper;

import com.htttql.crmmodule.crmrequest.domain.CustomerRequest;
import com.htttql.crmmodule.crmrequest.dto.CustomerRequestCreateRequest;
import com.htttql.crmmodule.crmrequest.dto.CustomerRequestResponse;

public final class CustomerRequestMapper {

    private CustomerRequestMapper() {
    }

    public static CustomerRequest toEntity(CustomerRequestCreateRequest request) {
        return CustomerRequest.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .customerNote(request.getCustomerNote())
                .source(request.getSource())
                .ipAddress(request.getIpAddress())
                .build();
    }

    public static CustomerRequestResponse toResponse(CustomerRequest entity) {
        return CustomerRequestResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .phoneNumber(entity.getPhoneNumber())
                .customerNote(entity.getCustomerNote())
                .source(entity.getSource())
                .ipAddress(entity.getIpAddress())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
