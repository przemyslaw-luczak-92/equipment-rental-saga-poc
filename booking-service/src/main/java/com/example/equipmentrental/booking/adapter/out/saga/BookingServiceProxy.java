package com.example.equipmentrental.booking.adapter.out.saga;

import com.example.equipmentrental.contracts.booking.ConfirmBookingCommand;
import com.example.equipmentrental.contracts.booking.ConfirmBookingFailedReply;
import com.example.equipmentrental.contracts.booking.ConfirmBookingSucceededReply;
import com.example.equipmentrental.contracts.booking.RejectBookingCommand;
import com.example.equipmentrental.contracts.booking.RejectBookingSucceededReply;
import io.eventuate.tram.sagas.simpledsl.CommandEndpoint;
import io.eventuate.tram.sagas.simpledsl.CommandEndpointBuilder;
import org.springframework.stereotype.Component;

@Component
public class BookingServiceProxy {

    private static final String CHANNEL = "booking-service";

    private final CommandEndpoint<ConfirmBookingCommand> confirmBooking =
            CommandEndpointBuilder
                    .forCommand(ConfirmBookingCommand.class)
                    .withChannel(CHANNEL)
                    .withReply(ConfirmBookingSucceededReply.class)
                    .withReply(ConfirmBookingFailedReply.class)
                    .build();

    private final CommandEndpoint<RejectBookingCommand> rejectBooking =
            CommandEndpointBuilder
                    .forCommand(RejectBookingCommand.class)
                    .withChannel(CHANNEL)
                    .withReply(RejectBookingSucceededReply.class)
                    .build();

    public CommandEndpoint<ConfirmBookingCommand> confirmBooking() {
        return confirmBooking;
    }

    public CommandEndpoint<RejectBookingCommand> rejectBooking() {
        return rejectBooking;
    }
}