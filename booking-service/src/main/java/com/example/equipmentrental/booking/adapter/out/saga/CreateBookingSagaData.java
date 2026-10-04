package com.example.equipmentrental.booking.adapter.out.saga;

import com.example.equipmentrental.contracts.booking.RejectBookingCommand;

import java.util.UUID;

public class CreateBookingSagaData {

    private UUID bookingId;
    private String customerId;
    private String equipmentId;
    private int quantity;
    private RejectBookingCommand.Reason reasonCode;

    public CreateBookingSagaData() {
    }

    public CreateBookingSagaData(UUID bookingId, String customerId, String equipmentId, int quantity) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.equipmentId = equipmentId;
        this.quantity = quantity;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public RejectBookingCommand.Reason getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(RejectBookingCommand.Reason reasonCode) {
        this.reasonCode = reasonCode;
    }
}