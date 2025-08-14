package com.htttql.crmmodule.crmrequest.repository;

import com.htttql.crmmodule.crmrequest.domain.CustomerRequest;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRequestRepository extends JpaRepository<CustomerRequest, UUID> {
    boolean existsByPhoneNumberAndCreatedAtAfter(String phoneNumber, Instant createdAt);

    boolean existsByIpAddressAndCreatedAtAfter(String ipAddress, Instant createdAt);
}
