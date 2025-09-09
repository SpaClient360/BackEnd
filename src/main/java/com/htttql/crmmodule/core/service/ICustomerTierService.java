package com.htttql.crmmodule.core.service;

import com.htttql.crmmodule.core.dto.CustomerResponse;

public interface ICustomerTierService {

    CustomerResponse refreshCustomerTier(Long customerId);

    void refreshAllCustomerTiers();

    void refreshCustomerTierInternal(Long customerId);
}
