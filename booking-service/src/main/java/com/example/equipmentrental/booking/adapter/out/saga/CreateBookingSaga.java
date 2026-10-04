package com.example.equipmentrental.booking.adapter.out.saga;

import com.example.equipmentrental.contracts.booking.ConfirmBookingCommand;
import com.example.equipmentrental.contracts.booking.ConfirmBookingFailedReply;
import com.example.equipmentrental.contracts.booking.RejectBookingCommand;
import com.example.equipmentrental.contracts.inventory.ReleaseStockCommand;
import com.example.equipmentrental.contracts.inventory.ReserveStockCommand;
import com.example.equipmentrental.contracts.inventory.ReserveStockFailedReply;
import io.eventuate.tram.sagas.orchestration.SagaDefinition;
import io.eventuate.tram.sagas.simpledsl.SimpleSaga;
import org.springframework.stereotype.Component;

@Component
public class CreateBookingSaga implements SimpleSaga<CreateBookingSagaData> {

    private final SagaDefinition<CreateBookingSagaData> sagaDefinition;

    public CreateBookingSaga(InventoryServiceProxy inventoryService, BookingServiceProxy bookingService) {
        this.sagaDefinition = step()
                .withCompensation(
                        bookingService.rejectBooking(),
                        this::rejectBooking
                )
                .step()
                .invokeParticipant(
                        inventoryService.reserveStock(),
                        this::reserveStock
                )
                .onReply(
                        ReserveStockFailedReply.class,
                        this::onReserveStockFailed
                )
                .withCompensation(
                        inventoryService.releaseStock(),
                        this::releaseStock
                )
                .step()
                .invokeParticipant(
                        bookingService.confirmBooking(),
                        this::confirmBooking
                )
                .onReply(
                        ConfirmBookingFailedReply.class,
                        this::onConfirmBookingFailed
                )
                .build();
    }

    @Override
    public SagaDefinition<CreateBookingSagaData> getSagaDefinition() {
        return sagaDefinition;
    }

    private ReserveStockCommand reserveStock(CreateBookingSagaData data) {
        return new ReserveStockCommand(
                data.getBookingId(),
                data.getEquipmentId(),
                data.getQuantity()
        );
    }

    private ConfirmBookingCommand confirmBooking(CreateBookingSagaData data) {
        return new ConfirmBookingCommand(data.getBookingId());
    }

    private ReleaseStockCommand releaseStock(CreateBookingSagaData data) {
        return new ReleaseStockCommand(data.getBookingId());
    }

    private RejectBookingCommand rejectBooking(CreateBookingSagaData data) {
        return new RejectBookingCommand(data.getBookingId(), data.getReasonCode());
    }

    private void onReserveStockFailed(CreateBookingSagaData data, ReserveStockFailedReply reply) {
        data.setReasonCode(switch (reply.reason()) {
            case UNKNOWN_EQUIPMENT -> RejectBookingCommand.Reason.UNKNOWN_EQUIPMENT;
            case INSUFFICIENT_STOCK -> RejectBookingCommand.Reason.INSUFFICIENT_STOCK;
            case HOLD_CONFLICT -> RejectBookingCommand.Reason.HOLD_CONFLICT;
        });
    }

    private void onConfirmBookingFailed(CreateBookingSagaData data, ConfirmBookingFailedReply reply) {
        data.setReasonCode(switch (reply.reason()) {
            case CUSTOMER_LIMIT_EXCEEDED -> RejectBookingCommand.Reason.CUSTOMER_LIMIT_EXCEEDED;
        });
    }
}