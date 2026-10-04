package com.example.equipmentrental.booking.application.port.out;

import com.example.equipmentrental.booking.domain.CustomerQuota;

import java.util.Optional;

public interface CustomerQuotaRepository {

    Optional<CustomerQuota> findByCustomerId(String customerId);

    Optional<CustomerQuota> findByCustomerIdForUpdate(String customerId);

    void save(CustomerQuota customerQuota);
}