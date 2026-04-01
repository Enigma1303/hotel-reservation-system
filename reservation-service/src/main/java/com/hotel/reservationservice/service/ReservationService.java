package com.hotel.reservationservice.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hotel.reservationservice.client.CustomerClient;
import com.hotel.reservationservice.client.RoomClient;
import com.hotel.reservationservice.dto.ReservationRequest;
import com.hotel.reservationservice.dto.ReservationResponse;
import com.hotel.reservationservice.dto.RoomDto;
import com.hotel.reservationservice.entity.Reservation;
import com.hotel.reservationservice.repository.ReservationRepository;

import feign.FeignException;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomClient roomClient;
    private final CustomerClient customerClient;

    public ReservationService(ReservationRepository reservationRepository, RoomClient roomClient, CustomerClient customerClient) {
        this.reservationRepository = reservationRepository;
        this.roomClient = roomClient;
        this.customerClient = customerClient;
    }

    public ReservationResponse createReservation(ReservationRequest request)
    {
        try
        {
            customerClient.getCustomerById(request.getCustomerId());
        } 
        catch (FeignException.NotFound e) {
            throw new RuntimeException("Customer not found");

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
