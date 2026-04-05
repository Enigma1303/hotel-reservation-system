package com.hotel.notificationservice.event;

import com.hotel.notificationservice.dto.EmailDto;
import com.hotel.notificationservice.entity.Notification;
import com.hotel.notificationservice.entity.ProcessedEvent;
import com.hotel.notificationservice.repository.ProcessedEventRepository;
import com.hotel.notificationservice.service.EmailTemplateService;
import com.hotel.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;
    private final ProcessedEventRepository processedEventRepository;
    private final EmailTemplateService emailTemplateService;

    public NotificationEventConsumer(NotificationService notificationService,
                                      ProcessedEventRepository processedEventRepository,
                                      EmailTemplateService emailTemplateService) {
        this.notificationService = notificationService;
        this.processedEventRepository = processedEventRepository;
        this.emailTemplateService = emailTemplateService;
    }

    @KafkaListener(topics = "reservation-events",
                   groupId = "notification-service-group",
                   containerFactory = "reservationKafkaListenerContainerFactory")
    public void handleReservationCreatedEvent(ReservationCreatedEvent event) {
        log.info("Received ReservationCreatedEvent for reservationId: {}",
            event.getPayload().getReservationId());

        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.warn("Duplicate ReservationCreatedEvent detected, discarding eventId: {}",
                event.getEventId());
            return;
        }

        EmailDto emailDto = emailTemplateService.reservationConfirmed(
            event.getPayload().getReservationId(),
            event.getPayload().getRoomId());

        emailDto.setTo(event.getPayload().getCustomerEmail());

        Notification notification = notificationService.createNotification(
            emailDto.getBody(),
            emailDto.getSubject(),
            emailDto.getTo(),
            Notification.NotificationType.EMAIL);

        notificationService.sendNotification(notification, emailDto);

        processedEventRepository.save(
            new ProcessedEvent(event.getEventId(), LocalDateTime.now()));

        log.info("ReservationCreatedEvent processed for reservationId: {}",
            event.getPayload().getReservationId());
    }

    @KafkaListener(topics = "payment-events",
                   groupId = "notification-service-group",
                   containerFactory = "paymentKafkaListenerContainerFactory")
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("Received PaymentCompletedEvent for reservationId: {} status: {}",
            event.getPayload().getReservationId(),
            event.getPayload().getStatus());

        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.warn("Duplicate PaymentCompletedEvent detected, discarding eventId: {}",
                event.getEventId());
            return;
        }

        EmailDto emailDto;
        if ("SUCCESS".equals(event.getPayload().getStatus())) {
            emailDto = emailTemplateService.paymentSuccess(
                event.getPayload().getReservationId());

            emailDto.setTo(event.getPayload().getCustomerEmail());
        } else {
            emailDto = emailTemplateService.paymentFailed(
                event.getPayload().getReservationId());
            emailDto.setTo(event.getPayload().getCustomerEmail());
        }
        log.info("Payment email received: {}", event.getPayload().getCustomerEmail());

        Notification notification = notificationService.createNotification(
            emailDto.getBody(),
            emailDto.getSubject(),
            emailDto.getTo(),
            Notification.NotificationType.EMAIL);

        notificationService.sendNotification(notification, emailDto);

        processedEventRepository.save(
            new ProcessedEvent(event.getEventId(), LocalDateTime.now()));

        log.info("PaymentCompletedEvent processed for reservationId: {}",
            event.getPayload().getReservationId());
    }
}