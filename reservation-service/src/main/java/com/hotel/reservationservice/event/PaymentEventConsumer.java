package com.hotel.reservationservice.event;

import com.hotel.reservationservice.client.RoomClient;
import com.hotel.reservationservice.entity.ProcessedEvent;
import com.hotel.reservationservice.entity.Reservation;
import com.hotel.reservationservice.repository.ProcessedEventRepository;
import com.hotel.reservationservice.repository.ReservationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

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

        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.warn("Duplicate event detected, discarding eventId: {}", event.getEventId());
            return;
        }

        if (event.getPayload() == null || event.getPayload().getReservationId() == null) {
            log.error("Invalid event payload, missing reservationId for eventId: {}", event.getEventId());
            return;
        }

        Reservation reservation = reservationRepository
                .findById(event.getPayload().getReservationId())
                .orElse(null);

        if (reservation == null) {
            log.error("Reservation not found for id: {}", event.getPayload().getReservationId());
            return;
        }

        if (reservation.getStatus() == Reservation.ReservationStatus.CONFIRMED ||
                reservation.getStatus() == Reservation.ReservationStatus.FAILED) {
            log.warn("Reservation {} already in terminal state, discarding event", reservation.getId());
            return;
        }

        if ("SUCCESS".equals(event.getPayload().getStatus())) {
            reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
            log.info("Payment successful for reservationId: {}", reservation.getId());
        } else {
            reservation.setStatus(Reservation.ReservationStatus.FAILED);
            log.warn("Payment failed for reservationId: {}, restoring room availability", reservation.getId());
            roomClient.updateAvailability(
                    reservation.getRoomId(),
                    Map.of("availability", true)
            );
        }

        reservationRepository.save(reservation);

        processedEventRepository.save(
                new ProcessedEvent(event.getEventId(), LocalDateTime.now())
        );

        log.info("Reservation {} updated to status: {}", reservation.getId(), reservation.getStatus());
    }
}