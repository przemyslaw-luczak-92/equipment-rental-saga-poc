package com.example.equipmentrental.booking.adapter.out.saga;

import com.example.equipmentrental.booking.application.port.out.CreateBookingSagaStarter;
import com.example.equipmentrental.booking.domain.Booking;
import io.eventuate.tram.sagas.orchestration.SagaInstanceFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class EventuateCreateBookingSagaStarter implements CreateBookingSagaStarter {

    private final SagaInstanceFactory sagaInstanceFactory;
    private final CreateBookingSaga createBookingSaga;

    public EventuateCreateBookingSagaStarter(SagaInstanceFactory sagaInstanceFactory, CreateBookingSaga createBookingSaga) {
        this.sagaInstanceFactory = sagaInstanceFactory;
        this.createBookingSaga = createBookingSaga;
    }

    @Override
    public void start(Booking booking) {
        Objects.requireNonNull(
                booking,
                "booking must not be null"
        );

        CreateBookingSagaData sagaData =
                new CreateBookingSagaData(
                        booking.bookingId(),
                        booking.customerId(),
                        booking.equipmentId(),
                        booking.quantity()
                );

        sagaInstanceFactory.create(
                createBookingSaga,
                sagaData
        );
    }
}