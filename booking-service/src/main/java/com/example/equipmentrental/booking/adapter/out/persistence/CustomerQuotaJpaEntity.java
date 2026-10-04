package com.example.equipmentrental.booking.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customer_quota")
public class CustomerQuotaJpaEntity {

    @Id
    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(name = "booking_limit", nullable = false)
    private int bookingLimit;

    @Column(name = "confirmed_count", nullable = false)
    private int confirmedCount;

    protected CustomerQuotaJpaEntity() {
        // Konstruktor wymagany przez JPA.
    }

    public CustomerQuotaJpaEntity(String customerId, int bookingLimit, int confirmedCount) {
        this.customerId = customerId;
        this.bookingLimit = bookingLimit;
        this.confirmedCount = confirmedCount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public int getBookingLimit() {
        return bookingLimit;
    }

    public int getConfirmedCount() {
        return confirmedCount;
    }
}