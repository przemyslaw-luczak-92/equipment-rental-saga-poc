package com.example.equipmentrental.booking.application.port.out;

import com.example.equipmentrental.booking.domain.Booking;

public interface CreateBookingSagaStarter {

    void start(Booking booking);
}