package com.hotel.reservationservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.hotel.reservationservice.client.ExternalServiceClient;
import com.hotel.reservationservice.dto.CustomerDto;
import com.hotel.reservationservice.dto.ReservationRequest;
import com.hotel.reservationservice.dto.ReservationResponse;
import com.hotel.reservationservice.dto.RoomDto;
import com.hotel.reservationservice.entity.Reservation;
import com.hotel.reservationservice.event.ReservationCreatedEvent;
import com.hotel.reservationservice.event.ReservationEventProducer;
import com.hotel.reservationservice.repository.ReservationRepository;

import feign.FeignException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    
    private final ExternalServiceClient externalServiceClient;
    private final ReservationEventProducer eventProducer;
    

    public ReservationService(ReservationRepository reservationRepository, ExternalServiceClient externalServiceClient, ReservationEventProducer eventProducer) {
        this.reservationRepository = reservationRepository;
        this.externalServiceClient = externalServiceClient;
        this.eventProducer = eventProducer;
    }
     

    public ReservationResponse createReservation(ReservationRequest request)
    {
       log.info("Creating reservation for customerId: {} roomId: {}",
            request.getCustomerId(), request.getRoomId());
       CustomerDto customerDto;
try 
{
    customerDto = externalServiceClient.fetchCustomer(request.getCustomerId());
} 
catch (FeignException.NotFound e)
{
    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
}

        RoomDto roomDto;
        try{
            roomDto=externalServiceClient.fetchRoom(request.getRoomId());
        }
        catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Room not found");
        }

        if(roomDto==null||!roomDto.getAvailability())
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Room is not available");
        }
      
        externalServiceClient.updateRoomAvailability(request.getRoomId(), false);

        Reservation reservation = new Reservation();
        reservation.setCustomerId(request.getCustomerId());
        reservation.setRoomId(request.getRoomId());
        reservation.setStatus(Reservation.ReservationStatus.PENDING);
        Reservation saved = reservationRepository.save(reservation);
        

        log.info("Reservation created with id: {} status: PENDING", saved.getId());

        ReservationCreatedEvent event= new ReservationCreatedEvent(
            UUID.randomUUID().toString(),
            "RESERVATION_CREATED",
            LocalDateTime.now(),
            new ReservationCreatedEvent.Payload(saved.getId(), saved.getCustomerId(), saved.getRoomId(),customerDto.getEmail())
             
        );
        
        eventProducer.publishReservationCreatedEvent(event);

        log.info("Publishing reservation event with email: {}", customerDto.getEmail());
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

        externalServiceClient.updateRoomAvailability(reservation.getRoomId(), true);

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        Reservation updated = reservationRepository.save(reservation);
         log.info("Reservation {} cancelled successfully", id);
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
