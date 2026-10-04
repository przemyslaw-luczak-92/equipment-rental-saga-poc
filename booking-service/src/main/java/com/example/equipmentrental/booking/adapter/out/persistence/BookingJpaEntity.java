package com.example.equipmentrental.booking.adapter.out.persistence;

import com.example.equipmentrental.booking.domain.BookingRejectionReason;
import com.example.equipmentrental.booking.domain.BookingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "booking")
public class BookingJpaEntity {

    @Id
    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId;

    @Column(name = "equipment_id", nullable = false, length = 100)
    private String equipmentId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "rejection_reason", length = 50)
    private BookingRejectionReason rejectionReason;

    protected BookingJpaEntity() {
        // Konstruktor wymagany przez JPA.
    }

    public BookingJpaEntity(
            UUID bookingId,
            String customerId,
            String equipmentId,
            int quantity,
            BookingStatus status,
            BookingRejectionReason rejectionReason) {

        this.bookingId = bookingId;
        this.customerId = customerId;
        this.equipmentId = equipmentId;
        this.quantity = quantity;
        this.status = status;
        this.rejectionReason = rejectionReason;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public BookingRejectionReason getRejectionReason() {
        return rejectionReason;
    }
}