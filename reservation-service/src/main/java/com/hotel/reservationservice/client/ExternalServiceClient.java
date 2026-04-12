package com.hotel.reservationservice.client;

import com.hotel.reservationservice.dto.CustomerDto;
import com.hotel.reservationservice.dto.PaymentDto;
import com.hotel.reservationservice.dto.RoomDto;
import com.hotel.reservationservice.exception.RoomNotFoundException;
import com.hotel.reservationservice.exception.RoomUnavailableException;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@Component
public class ExternalServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalServiceClient.class);

    private final CustomerClient customerClient;
    private final RoomClient roomClient;
    private final PaymentClient paymentClient;

    public ExternalServiceClient(CustomerClient customerClient,
                                  RoomClient roomClient,
                                  PaymentClient paymentClient
                            ) {
        this.customerClient = customerClient;
        this.roomClient = roomClient;
        this.paymentClient = paymentClient;
      
    }

    @CircuitBreaker(name = "customerServiceCB", fallbackMethod = "customerFallback")
    public CustomerDto fetchCustomer(Long customerId) {
        return customerClient.getCustomerById(customerId);
    }

    public CustomerDto customerFallback(Long customerId, Throwable t) {

    log.error("Customer fallback triggered for customerId: {} error: {}", customerId, t.getMessage());

    if (t instanceof feign.FeignException feignEx && feignEx.status() == 404) {
        throw new com.hotel.reservationservice.exception.CustomerNotFoundException(customerId);
    }

    if (t.getCause() instanceof feign.FeignException feignEx && feignEx.status() == 404) {
        throw new com.hotel.reservationservice.exception.CustomerNotFoundException(customerId);
    }

    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
        "Customer service temporarily unavailable");
}

    @CircuitBreaker(name = "roomServiceCB", fallbackMethod = "roomFallback")
    public RoomDto fetchRoom(Long roomId) {
        return roomClient.getRoomById(roomId);
    }

    public RoomDto roomFallback(Long roomId, Throwable t) {

    log.error("Room fallback triggered for roomId: {} error: {}", roomId, t.getMessage());


    if (t instanceof feign.FeignException feignEx && feignEx.status() == 404) {
        throw new com.hotel.reservationservice.exception.RoomNotFoundException(roomId);
    }

    if (t.getCause() instanceof feign.FeignException feignEx && feignEx.status() == 404) {
        throw new com.hotel.reservationservice.exception.RoomNotFoundException(roomId);
    }

    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
        "Room service temporarily unavailable");
}

    @CircuitBreaker(name = "roomServiceCB", fallbackMethod = "updateRoomFallback")
    public void updateRoomAvailability(Long roomId, boolean availability) {
        roomClient.updateAvailability(roomId, Map.of("availability", availability));
    }

    public void updateRoomFallback(Long roomId, boolean availability, Throwable t) {
        log.error("Room service unavailable when updating roomId: {}", roomId);
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
            "Room service temporarily unavailable");
    }

    @CircuitBreaker(name = "roomServiceCB", fallbackMethod = "bookRoomFallback")
public void bookRoom(Long roomId) {
    try {
        roomClient.bookRoom(roomId);
    } catch (FeignException e) {
        if (e.status() == 404) {
            throw new RoomNotFoundException(roomId);
        }
        if (e.status() == 409) {
            throw new RoomUnavailableException();
        }
        throw e; 
    }
}

   public void bookRoomFallback(Long roomId, Throwable t) {
    if (t instanceof RoomUnavailableException || t instanceof RoomNotFoundException) {
        throw (RuntimeException) t;
    }

    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
        "Room service temporarily unavailable");
}

    @CircuitBreaker(name = "paymentServiceCB", fallbackMethod = "paymentServiceFallback")
public PaymentDto fetchPayment(Long reservationId) {
    return paymentClient.getPaymentByReservationId(reservationId);
}

public PaymentDto paymentServiceFallback(Long reservationId, Throwable t) {
    log.warn("Payment service unavailable for reservationId: {} error: {}",
        reservationId, t.getMessage());
    PaymentDto fallback = new PaymentDto();
    fallback.setStatus("Payment temporarily unavailable, reservation queued");
    return fallback;
}
}