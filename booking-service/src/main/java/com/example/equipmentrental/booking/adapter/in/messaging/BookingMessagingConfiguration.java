package com.example.equipmentrental.booking.adapter.in.messaging;

import io.eventuate.tram.sagas.participant.SagaCommandDispatcher;
import io.eventuate.tram.sagas.participant.SagaCommandDispatcherFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class BookingMessagingConfiguration {

    @Bean(initMethod = "initialize")
    public SagaCommandDispatcher bookingCommandDispatcher(
            BookingCommandHandler commandHandler,
            SagaCommandDispatcherFactory dispatcherFactory) {

        return dispatcherFactory.make(
                "bookingCommandDispatcher",
                commandHandler.commandHandlers()
        );
    }
}