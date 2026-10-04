package com.example.equipmentrental.booking.adapter.in.messaging;

import com.example.equipmentrental.booking.application.port.in.ConfirmBookingResult;
import com.example.equipmentrental.booking.application.port.in.ConfirmBookingUseCase;
import com.example.equipmentrental.booking.application.port.in.RejectBookingResult;
import com.example.equipmentrental.booking.application.port.in.RejectBookingUseCase;
import com.example.equipmentrental.booking.domain.BookingRejectionReason;
import com.example.equipmentrental.contracts.booking.ConfirmBookingCommand;
import com.example.equipmentrental.contracts.booking.ConfirmBookingFailedReply;
import com.example.equipmentrental.contracts.booking.ConfirmBookingSucceededReply;
import com.example.equipmentrental.contracts.booking.RejectBookingCommand;
import com.example.equipmentrental.contracts.booking.RejectBookingSucceededReply;
import io.eventuate.tram.commands.consumer.CommandHandlers;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.messaging.common.Message;
import io.eventuate.tram.sagas.participant.SagaCommandHandlersBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withFailure;
import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withSuccess;

@Component
public class BookingCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(BookingCommandHandler.class);

    private static final String COMMAND_CHANNEL = "booking-service";

    private final ConfirmBookingUseCase confirmBookingUseCase;
    private final RejectBookingUseCase rejectBookingUseCase;

    public BookingCommandHandler(ConfirmBookingUseCase confirmBookingUseCase, RejectBookingUseCase rejectBookingUseCase) {
        this.confirmBookingUseCase = confirmBookingUseCase;
        this.rejectBookingUseCase = rejectBookingUseCase;
    }

    public CommandHandlers commandHandlers() {
        return SagaCommandHandlersBuilder
                .fromChannel(COMMAND_CHANNEL)
                .onMessage(
                        ConfirmBookingCommand.class,
                        this::confirmBooking
                )
                .onMessage(
                        RejectBookingCommand.class,
                        this::rejectBooking
                )
                .build();
    }

    private Message confirmBooking(CommandMessage<ConfirmBookingCommand> commandMessage) {
        ConfirmBookingCommand command = commandMessage.getCommand();

        ConfirmBookingResult result = confirmBookingUseCase.confirm(command.bookingId());

        log.info(
                "CONFIRM_BOOKING_PROCESSED bookingId={} result={}",
                command.bookingId(),
                result
        );

        return switch (result) {
            case CONFIRMED ->
                    withSuccess(
                            new ConfirmBookingSucceededReply()
                    );

            case CUSTOMER_LIMIT_EXCEEDED ->
                    withFailure(
                            new ConfirmBookingFailedReply(
                                    ConfirmBookingFailedReply.Reason
                                            .CUSTOMER_LIMIT_EXCEEDED
                            )
                    );
        };
    }

    private Message rejectBooking(CommandMessage<RejectBookingCommand> commandMessage) {

        RejectBookingCommand command = commandMessage.getCommand();

        RejectBookingResult result = rejectBookingUseCase.reject(
                command.bookingId(),
                toDomain(command.reason())
        );

        log.info(
                "REJECT_BOOKING_PROCESSED bookingId={} reason={} result={}",
                command.bookingId(),
                command.reason(),
                result
        );

        return withSuccess(new RejectBookingSucceededReply());
    }

    private BookingRejectionReason toDomain(RejectBookingCommand.Reason reason) {

        return switch (reason) {
            case UNKNOWN_EQUIPMENT -> BookingRejectionReason.UNKNOWN_EQUIPMENT;
            case INSUFFICIENT_STOCK -> BookingRejectionReason.INSUFFICIENT_STOCK;
            case HOLD_CONFLICT -> BookingRejectionReason.HOLD_CONFLICT;
            case CUSTOMER_LIMIT_EXCEEDED -> BookingRejectionReason.CUSTOMER_LIMIT_EXCEEDED;
        };
    }
}