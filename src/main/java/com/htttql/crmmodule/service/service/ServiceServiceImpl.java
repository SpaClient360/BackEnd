package com.htttql.crmmodule.service.service;

import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import com.htttql.crmmodule.service.dto.ServiceRequest;
import com.htttql.crmmodule.service.dto.ServiceResponse;
import com.htttql.crmmodule.service.entity.SpaService;
import com.htttql.crmmodule.service.repository.IServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("serviceService")
@RequiredArgsConstructor
public class ServiceServiceImpl implements IServiceService {

    private final IServiceRepository serviceRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceResponse> getAllServices(Pageable pageable) {
        Page<SpaService> services = serviceRepository.findAll(pageable);
        return services.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceResponse> getAllServicesByActiveStatus(Pageable pageable, Boolean isActive) {
        if (isActive == null) {
            return getAllServices(pageable);
        }

        Page<SpaService> allServices = serviceRepository.findAll(pageable);
        var filteredServices = allServices.getContent().stream()
                .filter(service -> service.getIsActive().equals(isActive))
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                filteredServices,
                pageable,
                allServices.getTotalElements()).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Long id) {
        SpaService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));
        return toResponse(service);
    }

    @Override
    @Transactional
    public ServiceResponse createService(ServiceRequest request) {
        SpaService service = SpaService.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .basePrice(request.getPrice())
                .durationMin(request.getDurationMinutes())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        service = serviceRepository.save(service);
        return toResponse(service);
    }

    @Override
    @Transactional
    public ServiceResponse updateService(Long id, ServiceRequest request) {
        SpaService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "id", id));

        if (request.getCode() != null)
            service.setCode(request.getCode());
        if (request.getName() != null)
            service.setName(request.getName());
        if (request.getDescription() != null)
            service.setDescription(request.getDescription());
        if (request.getCategory() != null)
            service.setCategory(request.getCategory());
        if (request.getPrice() != null)
            service.setBasePrice(request.getPrice());
        if (request.getDurationMinutes() != null)
            service.setDurationMin(request.getDurationMinutes());
        if (request.getIsActive() != null)
            service.setIsActive(request.getIsActive());

        service = serviceRepository.save(service);
        return toResponse(service);
    }

    @Override
    @Transactional
    public void deleteService(Long id) {
        if (!serviceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Service", "id", id);
        }
        serviceRepository.deleteById(id);
    }

    private ServiceResponse toResponse(SpaService service) {
        return modelMapper.map(service, ServiceResponse.class);
    }
}
