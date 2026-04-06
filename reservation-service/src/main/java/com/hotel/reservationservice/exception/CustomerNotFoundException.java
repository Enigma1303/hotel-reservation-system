package com.hotel.reservationservice.exception;

public class CustomerNotFoundException extends BusinessException {
    public CustomerNotFoundException(Long id) {
        super("Customer not found with id: " + id, 404);
    }
}