package com.htttql.crmmodule.core.service;

import com.htttql.crmmodule.common.enums.TierCode;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import com.htttql.crmmodule.core.dto.CustomerResponse;
import com.htttql.crmmodule.core.entity.Customer;
import com.htttql.crmmodule.core.entity.Tier;
import com.htttql.crmmodule.core.repository.ICustomerRepository;
import com.htttql.crmmodule.core.repository.ITierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service("customerTierService")
@RequiredArgsConstructor
public class CustomerTierServiceImpl implements ICustomerTierService {

    private final ICustomerRepository customerRepository;
    private final ITierRepository tierRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public CustomerResponse refreshCustomerTier(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        refreshCustomerTierInternal(customer);

        // Re-fetch to get updated data
        customer = customerRepository.findById(customerId).get();
        return toResponse(customer);
    }

    @Override
    @Transactional
    public void refreshAllCustomerTiers() {
        Iterable<Customer> allCustomers = customerRepository.findAll();
        int updatedCount = 0;

        for (Customer customer : allCustomers) {
            try {
                if (refreshCustomerTierInternal(customer)) {
                    updatedCount++;
                }
            } catch (Exception e) {
                // Log error silently, don't fail the batch operation
            }
        }
    }

    @Override
    @Transactional
    public void refreshCustomerTierInternal(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        refreshCustomerTierInternal(customer);
    }

    private boolean refreshCustomerTierInternal(Customer customer) {
        BigDecimal totalSpent = customer.getTotalSpent() != null ? customer.getTotalSpent() : BigDecimal.ZERO;
        Integer totalPoints = customer.getTotalPoints() != null ? customer.getTotalPoints() : 0;

        TierCode appropriateTierCode = determineTierCode(totalSpent, totalPoints);
        Tier currentTier = customer.getTier();

        // Only update if tier has changed
        if (currentTier == null || !currentTier.getCode().equals(appropriateTierCode)) {
            Tier newTier = tierRepository.findByCode(appropriateTierCode)
                    .orElseThrow(() -> new RuntimeException("Tier not found: " + appropriateTierCode));

            // Update tier in database
            customer.setTier(newTier);

            // Update VIP flag based on tier
            boolean isVip = appropriateTierCode == TierCode.VIP;
            customer.setIsVip(isVip);

            customerRepository.save(customer);
            return true;
        }

        return false;
    }

    private TierCode determineTierCode(BigDecimal totalSpent, Integer totalPoints) {
        if (totalSpent.compareTo(BigDecimal.valueOf(TierCode.VIP.getMinSpent())) >= 0 &&
                totalPoints >= TierCode.VIP.getMinPoints()) {
            return TierCode.VIP;
        }

        if (totalSpent.compareTo(BigDecimal.valueOf(TierCode.GOLD.getMinSpent())) >= 0 &&
                totalPoints >= TierCode.GOLD.getMinPoints()) {
            return TierCode.GOLD;
        }

        if (totalSpent.compareTo(BigDecimal.valueOf(TierCode.SILVER.getMinSpent())) >= 0 &&
                totalPoints >= TierCode.SILVER.getMinPoints()) {
            return TierCode.SILVER;
        }

        return TierCode.REGULAR;
    }

    private CustomerResponse toResponse(Customer customer) {
        CustomerResponse response = modelMapper.map(customer, CustomerResponse.class);
        if (customer.getTier() != null) {
            response.setTierCode(customer.getTier().getCode().name());
            response.setTierName(customer.getTier().getCode().getDescription());
        }
        return response;
    }
}
