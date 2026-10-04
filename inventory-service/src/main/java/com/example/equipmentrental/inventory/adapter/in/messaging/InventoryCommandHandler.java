package com.example.equipmentrental.inventory.adapter.in.messaging;

import com.example.equipmentrental.contracts.inventory.ReleaseStockCommand;
import com.example.equipmentrental.contracts.inventory.ReleaseStockSucceededReply;
import com.example.equipmentrental.contracts.inventory.ReserveStockCommand;
import com.example.equipmentrental.contracts.inventory.ReserveStockFailedReply;
import com.example.equipmentrental.contracts.inventory.ReserveStockSucceededReply;
import com.example.equipmentrental.inventory.application.port.in.ReleaseStockResult;
import com.example.equipmentrental.inventory.application.port.in.ReleaseStockUseCase;
import com.example.equipmentrental.inventory.application.port.in.ReserveStockResult;
import com.example.equipmentrental.inventory.application.port.in.ReserveStockUseCase;
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
public class InventoryCommandHandler {

    private static final Logger log = LoggerFactory.getLogger(InventoryCommandHandler.class);

    private static final String COMMAND_CHANNEL = "inventory-service";

    private final ReserveStockUseCase reserveStockUseCase;
    private final ReleaseStockUseCase releaseStockUseCase;

    public InventoryCommandHandler(ReserveStockUseCase reserveStockUseCase, ReleaseStockUseCase releaseStockUseCase) {
        this.reserveStockUseCase = reserveStockUseCase;
        this.releaseStockUseCase = releaseStockUseCase;
    }

    public CommandHandlers commandHandlers() {
        return SagaCommandHandlersBuilder
                .fromChannel(COMMAND_CHANNEL)
                .onMessage(
                        ReserveStockCommand.class,
                        this::reserveStock
                )
                .onMessage(
                        ReleaseStockCommand.class,
                        this::releaseStock
                )
                .build();
    }

    private Message reserveStock(CommandMessage<ReserveStockCommand> commandMessage) {

        ReserveStockCommand command = commandMessage.getCommand();

        ReserveStockResult result = reserveStockUseCase.reserve(
                command.bookingId(),
                command.equipmentId(),
                command.quantity()
        );

        log.info(
                "RESERVE_STOCK_PROCESSED bookingId={} equipmentId={} quantity={} result={}",
                command.bookingId(),
                command.equipmentId(),
                command.quantity(),
                result
        );

        return switch (result) {
            case RESERVED -> withSuccess(new ReserveStockSucceededReply());

            case UNKNOWN_EQUIPMENT ->
                    reservationFailure(
                            ReserveStockFailedReply.Reason.UNKNOWN_EQUIPMENT
                    );

            case INSUFFICIENT_STOCK ->
                    reservationFailure(
                            ReserveStockFailedReply.Reason.INSUFFICIENT_STOCK
                    );

            case HOLD_CONFLICT ->
                    reservationFailure(
                            ReserveStockFailedReply.Reason.HOLD_CONFLICT
                    );
        };
    }

    private Message releaseStock(CommandMessage<ReleaseStockCommand> commandMessage) {

        ReleaseStockCommand command = commandMessage.getCommand();

        ReleaseStockResult result = releaseStockUseCase.release(command.bookingId());

        log.info(
                "RELEASE_STOCK_PROCESSED bookingId={} result={}",
                command.bookingId(),
                result
        );

        return withSuccess(new ReleaseStockSucceededReply());
    }

    private Message reservationFailure(ReserveStockFailedReply.Reason reason) {
        return withFailure(new ReserveStockFailedReply(reason));
    }
}