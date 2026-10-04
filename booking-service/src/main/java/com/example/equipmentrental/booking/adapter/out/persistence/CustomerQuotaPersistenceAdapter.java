package com.example.equipmentrental.booking.adapter.out.persistence;

import com.example.equipmentrental.booking.application.port.out.CustomerQuotaRepository;
import com.example.equipmentrental.booking.domain.CustomerQuota;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CustomerQuotaPersistenceAdapter implements CustomerQuotaRepository {

    private final SpringDataCustomerQuotaRepository repository;

    public CustomerQuotaPersistenceAdapter(SpringDataCustomerQuotaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<CustomerQuota> findByCustomerId(String customerId) {
        return repository
                .findById(customerId)
                .map(this::toDomain);
    }

    @Override
    public Optional<CustomerQuota> findByCustomerIdForUpdate(String customerId) {
        return repository
                .findByCustomerIdForUpdate(customerId)
                .map(this::toDomain);
    }

    @Override
    public void save(CustomerQuota customerQuota) {
        repository.save(toEntity(customerQuota));
    }

    private CustomerQuota toDomain(CustomerQuotaJpaEntity entity) {
        return new CustomerQuota(
                entity.getCustomerId(),
                entity.getBookingLimit(),
                entity.getConfirmedCount()
        );
    }

    private CustomerQuotaJpaEntity toEntity(CustomerQuota customerQuota) {
        return new CustomerQuotaJpaEntity(
                customerQuota.customerId(),
                customerQuota.limit(),
                customerQuota.confirmedCount()
        );
    }
}