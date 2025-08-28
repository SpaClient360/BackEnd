package com.htttql.crmmodule.core.service;

import com.htttql.crmmodule.core.dto.TierRequest;
import com.htttql.crmmodule.core.dto.TierResponse;
import com.htttql.crmmodule.core.entity.Tier;
import com.htttql.crmmodule.core.repository.ITierRepository;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementation of Tier Service
 */
@Service
@RequiredArgsConstructor
public class TierServiceImpl implements ITierService {

    private final ITierRepository tierRepository;

    @Override
    public Page<TierResponse> getAllTiers(Pageable pageable) {
        Page<Tier> tiers = tierRepository.findAll(pageable);
        return tiers.map(this::mapToResponse);
    }

    @Override
    public TierResponse getTierById(Long id) {
        Tier tier = tierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tier not found with id: " + id));
        return mapToResponse(tier);
    }

    @Override
    public TierResponse createTier(TierRequest request) {
        Tier tier = Tier.builder()
                .code(request.getCode())
                .minPoints(request.getMinPoints())
                .minSpent(request.getDiscountRate()) // Use discountRate as minSpent for now
                .build();

        Tier savedTier = tierRepository.save(tier);
        return mapToResponse(savedTier);
    }

    @Override
    public TierResponse updateTier(Long id, TierRequest request) {
        Tier tier = tierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tier not found with id: " + id));

        tier.setCode(request.getCode());
        tier.setMinPoints(request.getMinPoints());
        tier.setMinSpent(request.getDiscountRate()); // Use discountRate as minSpent for now

        Tier updatedTier = tierRepository.save(tier);
        return mapToResponse(updatedTier);
    }

    @Override
    public void deleteTier(Long id) {
        if (!tierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tier not found with id: " + id);
        }
        tierRepository.deleteById(id);
    }

    private TierResponse mapToResponse(Tier tier) {
        return TierResponse.builder()
                .tierId(tier.getTierId())
                .code(tier.getCode())
                .name(tier.getCode().toString()) // Use code as name
                .description("Tier " + tier.getCode()) // Generate description from code
                .minPoints(tier.getMinPoints())
                .discountRate(tier.getMinSpent()) // Use minSpent as discountRate
                .createdAt(tier.getCreatedAt())
                .updatedAt(tier.getUpdatedAt())
                .build();
    }
}
