package com.hotel.reservationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hotel.reservationservice.dto.CustomerDto;

@FeignClient(name = "customer-service")
public interface CustomerClient {
   
    //not uisng response entity as we are using feign client and it will
    //  handle the response for us
@GetMapping("/api/customers/{id}")
    CustomerDto getCustomerById(@PathVariable("id") Long id);

}
