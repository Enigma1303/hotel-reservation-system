package com.hotel.paymentservice.controller;

import com.hotel.paymentservice.dto.PaymentResponse;
import com.hotel.paymentservice.event.PaymentEventConsumer;
import com.hotel.paymentservice.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentEventConsumer paymentEventConsumer;

    public PaymentController(PaymentService paymentService,
                              PaymentEventConsumer paymentEventConsumer) {
        this.paymentService = paymentService;
        this.paymentEventConsumer = paymentEventConsumer;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<PaymentResponse> getPaymentByReservation(
            @PathVariable Long reservationId) {
        return ResponseEntity.ok(paymentService.getPaymentByReservationId(reservationId));
    }

    @PostMapping("/simulate-failure")
    public ResponseEntity<Map<String, String>> simulateFailure() {
        paymentEventConsumer.setFailureCounter(10);
        return ResponseEntity.ok(Map.of("message",
            "Payment-Service will fail for next 10 requests"));
    }
}