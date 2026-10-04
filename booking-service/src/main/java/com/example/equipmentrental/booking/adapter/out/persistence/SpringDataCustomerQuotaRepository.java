package com.example.equipmentrental.booking.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataCustomerQuotaRepository
        extends JpaRepository<CustomerQuotaJpaEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select customerQuota
            from CustomerQuotaJpaEntity customerQuota
            where customerQuota.customerId = :customerId
            """)
    Optional<CustomerQuotaJpaEntity> findByCustomerIdForUpdate(
            @Param("customerId") String customerId
    );
}