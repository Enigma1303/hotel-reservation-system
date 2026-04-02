package com.hotel.reservationservice.event;

import com.hotel.reservationservice.client.RoomClient;
import com.hotel.reservationservice.entity.ProcessedEvent;
import com.hotel.reservationservice.entity.Reservation;
import com.hotel.reservationservice.repository.ProcessedEventRepository;
import com.hotel.reservationservice.repository.ReservationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Map;

@Component
public class PaymentEventConsumer {

    private final ReservationRepository reservationRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final RoomClient roomClient;

    public PaymentEventConsumer(ReservationRepository reservationRepository,
                                 ProcessedEventRepository processedEventRepository,
                                 RoomClient roomClient) {
        this.reservationRepository = reservationRepository;
        this.processedEventRepository = processedEventRepository;
        this.roomClient = roomClient;
    }

    @KafkaListener(topics = "payment-events", groupId = "reservation-service-group")
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        // Idempotency check
        if (processedEventRepository.existsByEventId(event.getEventId())) {
            System.out.println("Duplicate event detected, discarding: " + event.getEventId());
            return;
        }

        Reservation reservation = reservationRepository
                .findById(event.getPayload().getReservationId())
                .orElse(null);

        if (reservation == null) {
            System.out.println("Reservation not found for id: "
                + event.getPayload().getReservationId());
            return;
        }

        // Skip if already in terminal state
        if (reservation.getStatus() == Reservation.ReservationStatus.CONFIRMED ||
            reservation.getStatus() == Reservation.ReservationStatus.FAILED) {
            System.out.println("Reservation already in terminal state, discarding event");
            return;
        }

        if ("SUCCESS".equals(event.getPayload().getStatus())) {
            reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
        } else {
            reservation.setStatus(Reservation.ReservationStatus.FAILED);
            // Restore room availability on payment failure
            roomClient.updateAvailability(
                reservation.getRoomId(),
                Map.of("availability", true));
        }

        reservationRepository.save(reservation);

        // Mark event as processed
        processedEventRepository.save(
            new ProcessedEvent(event.getEventId(), LocalDateTime.now()));

        System.out.println("Reservation " + reservation.getId()
            + " updated to: " + reservation.getStatus());
    }
}