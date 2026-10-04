package com.example.equipmentrental.booking.adapter.out.saga;

import com.example.equipmentrental.contracts.booking.ConfirmBookingCommand;
import com.example.equipmentrental.contracts.booking.ConfirmBookingFailedReply;
import com.example.equipmentrental.contracts.booking.ConfirmBookingSucceededReply;
import com.example.equipmentrental.contracts.booking.RejectBookingCommand;
import com.example.equipmentrental.contracts.inventory.ReleaseStockCommand;
import com.example.equipmentrental.contracts.inventory.ReleaseStockSucceededReply;
import com.example.equipmentrental.contracts.inventory.ReserveStockCommand;
import com.example.equipmentrental.contracts.inventory.ReserveStockFailedReply;
import com.example.equipmentrental.contracts.inventory.ReserveStockSucceededReply;
import io.eventuate.common.json.mapper.JSonMapper;
import io.eventuate.tram.commands.common.Command;
import io.eventuate.tram.commands.common.CommandReplyOutcome;
import io.eventuate.tram.commands.common.ReplyMessageHeaders;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.messaging.producer.MessageBuilder;
import io.eventuate.tram.sagas.orchestration.SagaActions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateBookingSagaTest {

    private static final UUID BOOKING_ID = UUID.randomUUID();

    private CreateBookingSaga saga;
    private CreateBookingSagaData data;

    @BeforeEach
    void setUp() {
        saga = new CreateBookingSaga(
                new InventoryServiceProxy(),
                new BookingServiceProxy()
        );

        data = new CreateBookingSagaData(
                BOOKING_ID,
                "customer-1",
                "camera",
                2
        );
    }

    @Test
    void shouldCompleteWhenAllParticipantsSucceed() {
        SagaActions<CreateBookingSagaData> reserve = saga.getSagaDefinition().start(data);

        assertCommand(
                reserve,
                "inventory-service",
                new ReserveStockCommand(BOOKING_ID, "camera", 2)
        );

        SagaActions<CreateBookingSagaData> confirm = reply(
                reserve,
                new ReserveStockSucceededReply(),
                CommandReplyOutcome.SUCCESS
        );

        assertCommand(
                confirm,
                "booking-service",
                new ConfirmBookingCommand(BOOKING_ID)
        );

        SagaActions<CreateBookingSagaData> completed = reply(
                confirm,
                new ConfirmBookingSucceededReply(),
                CommandReplyOutcome.SUCCESS
        );

        assertTrue(completed.isEndState());
        assertFalse(completed.isCompensating());
    }

    @Test
    void shouldRejectWithoutReleaseWhenReserveFails() {
        SagaActions<CreateBookingSagaData> reserve = saga.getSagaDefinition().start(data);

        SagaActions<CreateBookingSagaData> reject = reply(
                reserve,
                new ReserveStockFailedReply(
                        ReserveStockFailedReply.Reason.INSUFFICIENT_STOCK
                ),
                CommandReplyOutcome.FAILURE
        );

        assertCommand(
                reject,
                "booking-service",
                new RejectBookingCommand(
                        BOOKING_ID,
                        RejectBookingCommand.Reason.INSUFFICIENT_STOCK
                )
        );
    }

    @Test
    void shouldReleaseAndRejectWhenConfirmationFails() {
        SagaActions<CreateBookingSagaData> reserve = saga.getSagaDefinition().start(data);

        SagaActions<CreateBookingSagaData> confirm = reply(
                reserve,
                new ReserveStockSucceededReply(),
                CommandReplyOutcome.SUCCESS
        );

        SagaActions<CreateBookingSagaData> release = reply(
                confirm,
                new ConfirmBookingFailedReply(
                        ConfirmBookingFailedReply.Reason.CUSTOMER_LIMIT_EXCEEDED
                ),
                CommandReplyOutcome.FAILURE
        );

        assertCommand(
                release,
                "inventory-service",
                new ReleaseStockCommand(BOOKING_ID)
        );

        SagaActions<CreateBookingSagaData> reject = reply(
                release,
                new ReleaseStockSucceededReply(),
                CommandReplyOutcome.SUCCESS
        );

        assertCommand(
                reject,
                "booking-service",
                new RejectBookingCommand(
                        BOOKING_ID,
                        RejectBookingCommand.Reason.CUSTOMER_LIMIT_EXCEEDED
                )
        );
    }

    private SagaActions<CreateBookingSagaData> reply(SagaActions<CreateBookingSagaData> previousActions,
            Object reply, CommandReplyOutcome outcome) {
        Message message = MessageBuilder
                .withPayload(JSonMapper.toJson(reply))
                .withHeader(
                        ReplyMessageHeaders.REPLY_TYPE,
                        reply.getClass().getName()
                )
                .withHeader(
                        ReplyMessageHeaders.REPLY_OUTCOME,
                        outcome.name()
                )
                .build();

        return saga.getSagaDefinition().handleReply(
                saga.getSagaType(),
                "test-saga-id",
                previousActions.getUpdatedState().orElseThrow(),
                data,
                message
        );
    }

    private void assertCommand(SagaActions<CreateBookingSagaData> actions, String expectedChannel, Command expectedCommand) {
        assertEquals(1, actions.getCommands().size());

        var commandWithDestination = actions
                .getCommands()
                .get(0)
                .getCommandWithDestination();

        assertEquals(
                expectedChannel,
                commandWithDestination.getDestinationChannel()
        );

        assertEquals(
                expectedCommand,
                commandWithDestination.getCommand()
        );
    }
}