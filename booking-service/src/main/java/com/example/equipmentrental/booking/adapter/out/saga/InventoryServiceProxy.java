package com.example.equipmentrental.booking.adapter.out.saga;

import com.example.equipmentrental.contracts.inventory.ReleaseStockCommand;
import com.example.equipmentrental.contracts.inventory.ReleaseStockSucceededReply;
import com.example.equipmentrental.contracts.inventory.ReserveStockCommand;
import com.example.equipmentrental.contracts.inventory.ReserveStockFailedReply;
import com.example.equipmentrental.contracts.inventory.ReserveStockSucceededReply;
import io.eventuate.tram.sagas.simpledsl.CommandEndpoint;
import io.eventuate.tram.sagas.simpledsl.CommandEndpointBuilder;
import org.springframework.stereotype.Component;

@Component
public class InventoryServiceProxy {

    private static final String CHANNEL = "inventory-service";

    private final CommandEndpoint<ReserveStockCommand> reserveStock =
            CommandEndpointBuilder
                    .forCommand(ReserveStockCommand.class)
                    .withChannel(CHANNEL)
                    .withReply(ReserveStockSucceededReply.class)
                    .withReply(ReserveStockFailedReply.class)
                    .build();

    private final CommandEndpoint<ReleaseStockCommand> releaseStock =
            CommandEndpointBuilder
                    .forCommand(ReleaseStockCommand.class)
                    .withChannel(CHANNEL)
                    .withReply(ReleaseStockSucceededReply.class)
                    .build();

    public CommandEndpoint<ReserveStockCommand> reserveStock() {
        return reserveStock;
    }

    public CommandEndpoint<ReleaseStockCommand> releaseStock() {
        return releaseStock;
    }
}