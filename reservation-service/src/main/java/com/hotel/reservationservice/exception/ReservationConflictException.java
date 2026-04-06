package com.hotel.reservationservice.exception;

// ReservationConflictException
public class ReservationConflictException extends BusinessException {
    public ReservationConflictException() {
        super("Reservation cannot be cancelled", 409);
    }
}
