package com.hotel.reservationservice.exception;
public class RoomUnavailableException extends BusinessException {
    public RoomUnavailableException() {
        super("Room is not available", 409);
    }
}
