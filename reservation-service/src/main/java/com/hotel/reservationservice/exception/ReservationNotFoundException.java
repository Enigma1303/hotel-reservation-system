package com.hotel.reservationservice.exception;

public class ReservationNotFoundException extends BusinessException {
    public ReservationNotFoundException(Long id) {
        super("Reservation not found with id: " + id, 404);
    }
}