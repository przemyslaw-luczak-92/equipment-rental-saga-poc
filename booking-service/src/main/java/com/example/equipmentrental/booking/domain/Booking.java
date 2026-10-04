package com.example.equipmentrental.booking.domain;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Booking {

    private final UUID bookingId;
    private final String customerId;
    private final String equipmentId;
    private final int quantity;

    private BookingStatus status;
    private BookingRejectionReason rejectionReason;

    private Booking(UUID bookingId, String customerId, String equipmentId, int quantity, BookingStatus status,
            BookingRejectionReason rejectionReason) {

        this.bookingId = Objects.requireNonNull(
                bookingId,
                "bookingId must not be null"
        );

        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException(
                    "customerId must not be blank"
            );
        }

        if (equipmentId == null || equipmentId.isBlank()) {
            throw new IllegalArgumentException(
                    "equipmentId must not be blank"
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "quantity must be greater than zero"
            );
        }

        this.customerId = customerId;
        this.equipmentId = equipmentId;
        this.quantity = quantity;
        this.status = Objects.requireNonNull(
                status,
                "status must not be null"
        );

        if (status == BookingStatus.REJECTED
                && rejectionReason == null) {
            throw new IllegalArgumentException(
                    "Rejected booking must have a rejection reason"
            );
        }

        if (status != BookingStatus.REJECTED
                && rejectionReason != null) {
            throw new IllegalArgumentException(
                    "Only rejected booking can have a rejection reason"
            );
        }

        this.rejectionReason = rejectionReason;
    }

    public static Booking pending(UUID bookingId, String customerId, String equipmentId, int quantity) {
        return new Booking(
                bookingId,
                customerId,
                equipmentId,
                quantity,
                BookingStatus.PENDING,
                null
        );
    }

    public static Booking restore(UUID bookingId, String customerId, String equipmentId, int quantity,
            BookingStatus status, BookingRejectionReason rejectionReason) {

        return new Booking(
                bookingId,
                customerId,
                equipmentId,
                quantity,
                status,
                rejectionReason
        );
    }

    public boolean confirm() {
        if (status == BookingStatus.CONFIRMED) {
            return false;
        }
        if (status == BookingStatus.REJECTED) {
            throw new IllegalStateException(
                    "Rejected booking cannot be confirmed"
            );
        }

        status = BookingStatus.CONFIRMED;
        return true;
    }

    public boolean reject(BookingRejectionReason reason) {
        Objects.requireNonNull(reason, "reason must not be null");

        if (status == BookingStatus.REJECTED) {
            return false;
        }
        if (status == BookingStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Confirmed booking cannot be rejected"
            );
        }

        status = BookingStatus.REJECTED;
        rejectionReason = reason;
        return true;
    }

    public UUID bookingId() {
        return bookingId;
    }

    public String customerId() {
        return customerId;
    }

    public String equipmentId() {
        return equipmentId;
    }

    public int quantity() {
        return quantity;
    }

    public BookingStatus status() {
        return status;
    }

    public Optional<BookingRejectionReason> rejectionReason() {
        return Optional.ofNullable(rejectionReason);
    }
}