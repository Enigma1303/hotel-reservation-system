package com.hotel.paymentservice.service;

import com.hotel.paymentservice.dto.PaymentResponse;
import com.hotel.paymentservice.entity.Payment;
import com.hotel.paymentservice.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Payment not found"));
        return new PaymentResponse(payment.getId(),
            payment.getReservationId(), payment.getStatus());
    }

    public PaymentResponse getPaymentByReservationId(Long reservationId) {
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Payment not found"));
        return new PaymentResponse(payment.getId(),
            payment.getReservationId(), payment.getStatus());
    }
}