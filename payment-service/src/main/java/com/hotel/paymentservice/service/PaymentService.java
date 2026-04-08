package com.hotel.paymentservice.service;

import com.hotel.paymentservice.dto.PaymentResponse;
import com.hotel.paymentservice.entity.Payment;
import com.hotel.paymentservice.repository.PaymentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;
    private volatile boolean simulatingFailure = false;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public void activateFailureSimulation() {
        this.simulatingFailure = true;
        log.warn("Payment service failure simulation activated");
    }

    public void deactivateFailureSimulation() {
        this.simulatingFailure = false;
        log.info("Payment service failure simulation deactivated");
    }

    public PaymentResponse getPaymentById(Long id) {
        if (simulatingFailure) {
            log.error("Simulated failure on getPaymentById");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Payment service simulated failure");
        }
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Payment not found"));
        return new PaymentResponse(payment.getId(),
            payment.getReservationId(), payment.getStatus());
    }

    public PaymentResponse getPaymentByReservationId(Long reservationId) {
        if (simulatingFailure) {
            log.error("Simulated failure on getPaymentByReservationId");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Payment service simulated failure");
        }
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Payment not found"));
        return new PaymentResponse(payment.getId(),
            payment.getReservationId(), payment.getStatus());
    }
}
  

