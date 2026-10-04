package com.example.equipmentrental.booking.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerQuotaTest {

    @Test
    void shouldConfirmAnotherBookingWithinLimit() {
        CustomerQuota quota =
                new CustomerQuota("customer-normal", 2, 0);

        assertTrue(quota.tryConfirmAnotherBooking());
        assertEquals(1, quota.confirmedCount());
    }

    @Test
    void shouldNotExceedLimit() {
        CustomerQuota quota =
                new CustomerQuota("customer-normal", 2, 1);

        assertTrue(quota.tryConfirmAnotherBooking());
        assertFalse(quota.tryConfirmAnotherBooking());

        assertEquals(2, quota.confirmedCount());
    }

    @Test
    void shouldRejectCustomerWithZeroLimit() {
        CustomerQuota quota =
                new CustomerQuota("customer-compensation", 0, 0);

        assertFalse(quota.tryConfirmAnotherBooking());
        assertEquals(0, quota.confirmedCount());
    }

    @Test
    void shouldRejectNegativeLimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CustomerQuota("customer-normal", -1, 0)
        );
    }

    @Test
    void shouldRejectConfirmedCountAboveLimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CustomerQuota("customer-normal", 2, 3)
        );
    }
}