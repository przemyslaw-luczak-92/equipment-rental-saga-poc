package com.example.equipmentrental.inventory.adapter.in.messaging;

import io.eventuate.tram.sagas.participant.SagaCommandDispatcher;
import io.eventuate.tram.sagas.participant.SagaCommandDispatcherFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class InventoryMessagingConfiguration {

    @Bean(initMethod = "initialize")
    public SagaCommandDispatcher inventoryCommandDispatcher(
            InventoryCommandHandler commandHandler,
            SagaCommandDispatcherFactory dispatcherFactory) {

        return dispatcherFactory.make(
                "inventoryCommandDispatcher",
                commandHandler.commandHandlers()
        );
    }
}