package com.hotel.paymentservice.dto;

import com.hotel.paymentservice.entity.Payment;

public class PaymentResponse {
    private Long id;
    private Long reservationId;
    private Payment.PaymentStatus status;

    public PaymentResponse(Long id, Long reservationId, Payment.PaymentStatus status) {
        this.id = id;
        this.reservationId = reservationId;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Payment.PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(Payment.PaymentStatus status) {
        this.status = status;
    }
}