package com.hotel.reservationservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hotel.reservationservice.client.CustomerClient;
import com.hotel.reservationservice.client.RoomClient;
import com.hotel.reservationservice.dto.CustomerDto;
import com.hotel.reservationservice.dto.ReservationRequest;
import com.hotel.reservationservice.dto.ReservationResponse;
import com.hotel.reservationservice.dto.RoomDto;
import com.hotel.reservationservice.entity.Reservation;
import com.hotel.reservationservice.event.ReservationCreatedEvent;
import com.hotel.reservationservice.event.ReservationEventProducer;
import com.hotel.reservationservice.repository.ReservationRepository;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private final RoomClient roomClient;
    private final CustomerClient customerClient;
    private final ReservationEventProducer eventProducer;

    public ReservationService(ReservationRepository reservationRepository, RoomClient roomClient, CustomerClient customerClient, ReservationEventProducer eventProducer) {
        this.reservationRepository = reservationRepository;
        this.roomClient = roomClient;
        this.customerClient = customerClient;
        this.eventProducer = eventProducer;
    }
     


    public ReservationResponse createReservation(ReservationRequest request)
    {

       CustomerDto customerDto;
try 
{
    customerDto = customerClient.getCustomerById(request.getCustomerId());
} 
catch (FeignException.NotFound e)
{
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
}

        RoomDto roomDto;
        try{
            roomDto=roomClient.getRoomById(request.getRoomId());
        }
        catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Room not found");
        }

        if(roomDto==null||!roomDto.getAvailability())
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Room is not available");
        }
      
        roomClient.updateAvailability(request.getRoomId(), Map.of("availability", false));

        Reservation reservation = new Reservation();
        reservation.setCustomerId(request.getCustomerId());
        reservation.setRoomId(request.getRoomId());
        reservation.setStatus(Reservation.ReservationStatus.PENDING);
        Reservation saved = reservationRepository.save(reservation);

        ReservationCreatedEvent event= new ReservationCreatedEvent(
            UUID.randomUUID().toString(),
            "RESERVATION_CREATED",
            LocalDateTime.now(),
            new ReservationCreatedEvent.Payload(saved.getId(), saved.getCustomerId(), saved.getRoomId(),customerDto.getEmail())
             
        );
        log.info("Publishing reservation event with email: {}", customerDto.getEmail());
        eventProducer.publishReservationCreatedEvent(event);
        return toResponse(saved);
    }
 public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Reservation not found"));
        return toResponse(reservation);
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ReservationResponse cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Reservation not found"));

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Reservation is not in a cancellable state");
        }

        // Restore room availability
        roomClient.updateAvailability(reservation.getRoomId(), Map.of("availability", true));

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        Reservation updated = reservationRepository.save(reservation);
        return toResponse(updated);
    }

     private ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getCustomerId(),
            reservation.getRoomId(),
            reservation.getStatus(),
            reservation.getCreatedAt());
    }
}
