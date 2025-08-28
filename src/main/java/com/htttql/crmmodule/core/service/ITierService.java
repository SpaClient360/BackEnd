package com.htttql.crmmodule.core.service;

import com.htttql.crmmodule.core.dto.TierRequest;
import com.htttql.crmmodule.core.dto.TierResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Tier operations
 */
public interface ITierService {

    /**
     * Get all tiers with pagination
     */
    Page<TierResponse> getAllTiers(Pageable pageable);

    /**
     * Get tier by ID
     */
    TierResponse getTierById(Long id);

    /**
     * Create new tier
     */
    TierResponse createTier(TierRequest request);

    /**
     * Update tier
     */
    TierResponse updateTier(Long id, TierRequest request);

    /**
     * Delete tier
     */
    void deleteTier(Long id);
}
