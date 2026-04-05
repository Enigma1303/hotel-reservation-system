package com.hotel.reservationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hotel.reservationservice.dto.CustomerDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@FeignClient(name = "customer-service")
public interface CustomerClient {

    @GetMapping("/api/customers/{id}")
    @CircuitBreaker(name = "customerServiceCB", fallbackMethod = "customerFallback")
    CustomerDto getCustomerById(@PathVariable("id") Long id);

    // ✅ Fallback must match the original method's parameters + Throwable
    default CustomerDto customerFallback(Long id, Throwable t) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "Customer service temporarily unavailable");
    }
}