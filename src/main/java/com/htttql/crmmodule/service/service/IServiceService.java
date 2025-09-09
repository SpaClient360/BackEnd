package com.htttql.crmmodule.service.service;

import com.htttql.crmmodule.service.dto.ServiceRequest;
import com.htttql.crmmodule.service.dto.ServiceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IServiceService {

    Page<ServiceResponse> getAllServices(Pageable pageable);

    Page<ServiceResponse> getAllServicesByActiveStatus(Pageable pageable, Boolean isActive);

    ServiceResponse getServiceById(Long id);

    ServiceResponse createService(ServiceRequest request);

    ServiceResponse updateService(Long id, ServiceRequest request);

    void deleteService(Long id);
}
