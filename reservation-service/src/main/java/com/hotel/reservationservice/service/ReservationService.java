package com.hotel.reservationservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hotel.reservationservice.client.ExternalServiceClient;
import com.hotel.reservationservice.dto.*;
import com.hotel.reservationservice.entity.Reservation;
import com.hotel.reservationservice.event.ReservationCreatedEvent;
import com.hotel.reservationservice.event.ReservationEventProducer;
import com.hotel.reservationservice.exception.*;
import com.hotel.reservationservice.repository.ReservationRepository;

import feign.FeignException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final ExternalServiceClient externalServiceClient;
    private final ReservationEventProducer eventProducer;

    public ReservationService(ReservationRepository reservationRepository,
                              ExternalServiceClient externalServiceClient,
                              ReservationEventProducer eventProducer) {
        this.reservationRepository = reservationRepository;
        this.externalServiceClient = externalServiceClient;
        this.eventProducer = eventProducer;
    }

    public ReservationResponse createReservation(ReservationRequest request) {

        log.info("Creating reservation for customerId: {} roomId: {}",
                request.getCustomerId(), request.getRoomId());
        CustomerDto customerDto;
        try {
            customerDto = externalServiceClient.fetchCustomer(request.getCustomerId());
        } catch (FeignException e) {
            if (e.status() == 404) {
                log.error("Customer not found: {}", request.getCustomerId());
                throw new CustomerNotFoundException(request.getCustomerId());
            }
            log.error("Customer service error", e);
            throw e;
        }

        RoomDto roomDto;
        try {
            roomDto = externalServiceClient.fetchRoom(request.getRoomId());
        } catch (FeignException e) {
            if (e.status() == 404) {
                log.error("Room not found: {}", request.getRoomId());
                throw new RoomNotFoundException(request.getRoomId());
            }
            log.error("Room service error", e);
            throw e;
        }

     
        if (roomDto == null || !roomDto.getAvailability()) {
            log.error("Room not available: {}", request.getRoomId());
            throw new RoomUnavailableException();
        }

       
        externalServiceClient.updateRoomAvailability(request.getRoomId(), false);

      
        Reservation reservation = new Reservation();
        reservation.setCustomerId(request.getCustomerId());
        reservation.setRoomId(request.getRoomId());
        reservation.setStatus(Reservation.ReservationStatus.PENDING);

        Reservation saved = reservationRepository.save(reservation);

        log.info("Reservation created with id: {}", saved.getId());

        ReservationCreatedEvent event = new ReservationCreatedEvent(
                UUID.randomUUID().toString(),
                "RESERVATION_CREATED",
                LocalDateTime.now(),
                new ReservationCreatedEvent.Payload(
                        saved.getId(),
                        saved.getCustomerId(),
                        saved.getRoomId(),
                        customerDto.getEmail()
                )
        );

        eventProducer.publishReservationCreatedEvent(event);

        log.info("Reservation event published for id: {}", saved.getId());

        return toResponse(saved);
    }

    public ReservationResponse getReservationById(Long id) {
        log.info("Fetching reservation: {}", id);
        if(id == null) {
            log.error("Reservation id cannot be null");
            throw new IllegalArgumentException("Reservation id cannot be null");
        }
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Reservation not found: {}", id);
                    return new ReservationNotFoundException(id);
                });

        return toResponse(reservation);
    }

    public List<ReservationResponse> getAllReservations() {
        log.info("Fetching all reservations");

        return reservationRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ReservationResponse cancelReservation(Long id) {

        log.info("Cancelling reservation: {}", id);
        if(id == null) {
            log.error("Reservation id cannot be null");
            throw new IllegalArgumentException("Reservation id cannot be null");
        }
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            log.error("Reservation {} not cancellable", id);
            throw new ReservationConflictException();
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
                reservation.getCreatedAt()
        );
    }

    public ReservationDetailsResponse getReservationDetails(Long id) {

    log.info("Fetching details for reservationId: {}", id);
    if(id == null) {
        log.error("Reservation id cannot be null");
        throw new IllegalArgumentException("Reservation id cannot be null");
    }
    Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Reservation not found: {}", id);
                return new ReservationNotFoundException(id);
            });

    String customerName;
    try {
        CustomerDto customerDto = externalServiceClient.fetchCustomer(reservation.getCustomerId());
        customerName = customerDto.getName();
    } catch (FeignException e) {
        log.warn("Could not fetch customer for reservationId: {}", id);
        customerName = "Unavailable";
    }

    String roomType;
    try {
        RoomDto roomDto = externalServiceClient.fetchRoom(reservation.getRoomId());
        roomType = roomDto.getType();
    } catch (Exception e) {

    log.error("Error while fetching room: {}", e.getMessage());

    if (e instanceof FeignException feignEx) {
        if (feignEx.status() == 404) {
            throw new RoomNotFoundException(reservation.getRoomId());
        }
    }
    if (e.getCause() instanceof FeignException feignEx) {
        if (feignEx.status() == 404) {
            throw new RoomNotFoundException(reservation.getRoomId());
        }
    }

    throw e;
}
    String paymentStatus;
    try {
        PaymentDto paymentDto = externalServiceClient.fetchPayment(reservation.getId());
        paymentStatus = paymentDto.getStatus();
    } catch (FeignException e) {
        log.warn("Could not fetch payment for reservationId: {}", id);
        paymentStatus = "Payment temporarily unavailable";
    }

    log.info("Reservation details fetched successfully for id: {}", id);

    return new ReservationDetailsResponse(
            reservation.getId(),
            reservation.getStatus(),
            customerName,
            reservation.getRoomId(),
            roomType,
            paymentStatus,
            reservation.getCreatedAt()
    );
}
}