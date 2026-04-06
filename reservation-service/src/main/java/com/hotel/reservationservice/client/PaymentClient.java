package com.hotel.reservationservice.client;

import com.hotel.reservationservice.dto.PaymentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @GetMapping("/api/payments/reservation/{reservationId}")
    PaymentDto getPaymentByReservationId(@PathVariable("reservationId") Long reservationId);
}