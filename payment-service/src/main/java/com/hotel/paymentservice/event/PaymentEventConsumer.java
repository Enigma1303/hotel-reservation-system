package com.hotel.paymentservice.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hotel.paymentservice.entity.Payment;
import com.hotel.paymentservice.entity.ProcessedEvent;
import com.hotel.paymentservice.repository.PaymentRepository;
import com.hotel.paymentservice.repository.ProcessedEventRepository;

@Component
public class PaymentEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final PaymentRepository paymentRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    private static final String TOPIC = "payment-events";

    private int failureCount = 0;

    public PaymentEventConsumer(
            PaymentRepository paymentRepository,
            ProcessedEventRepository processedEventRepository,
            KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate
    ) {
        this.paymentRepository = paymentRepository;
        this.processedEventRepository = processedEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

  
    public void setFailureCounter(int failureCount) {
        this.failureCount = failureCount;
        log.warn(" Failure counter set to: {}", this.failureCount);
    }

    @KafkaListener(topics = "reservation-events", groupId = "payment-service-group")
    public void handleRegistrationCreatedEvent(ReservationCreatedEvent event) {

        log.info("Received event in payment. Email: {}", event.getPayload().getCustomerEmail());
        log.info("Current failureCount value: {}", failureCount);

        if (event == null || event.getPayload() == null || event.getPayload().getReservationId() == null) {
            log.error("Invalid event received, skipping processing. Event: {}", event);
            return;
        }

        if (processedEventRepository.existsByEventId(event.getEventId())) {
            log.warn("Duplicate event detected, discarding: {}", event.getEventId());
            return;
        }

        Long reservationId = event.getPayload().getReservationId();

        if (failureCount > 0) {
            failureCount--;

            log.error("Simulated payment failure for reservationId: {}. Remaining failures: {}",
                    reservationId, failureCount);

            Payment failedPayment = new Payment();
            failedPayment.setReservationId(reservationId);
            failedPayment.setStatus(Payment.PaymentStatus.FAILED);

            Payment savedPayment = paymentRepository.save(failedPayment);

        
            publishPaymentEvent(event, savedPayment);

            processedEventRepository.save(
                    new ProcessedEvent(event.getEventId(), LocalDateTime.now())
            );

            return; 
        }
        Payment payment = new Payment();
        payment.setReservationId(reservationId);
        payment.setStatus(Payment.PaymentStatus.PENDING);

        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment created successfully: {}", savedPayment.getId());

        // Random success/failure simulation
        boolean paymentSuccess = Math.random() > 0.3;

        log.info("Payment simulation result: {}", paymentSuccess ? "SUCCESS" : "FAILED");

        savedPayment.setStatus(
                paymentSuccess
                        ? Payment.PaymentStatus.SUCCESS
                        : Payment.PaymentStatus.FAILED
        );

        paymentRepository.save(savedPayment);

        publishPaymentEvent(event, savedPayment);

        processedEventRepository.save(
                new ProcessedEvent(event.getEventId(), LocalDateTime.now())
        );

        log.info("PaymentCompletedEvent published for reservationId: {}", reservationId);
    }

    private void publishPaymentEvent(ReservationCreatedEvent event, Payment payment) {

        PaymentCompletedEvent paymentCompletedEvent = new PaymentCompletedEvent(
                UUID.randomUUID().toString(),
                "PAYMENT_COMPLETED",
                LocalDateTime.now(),
                new PaymentCompletedEvent.Payload(
                        event.getPayload().getReservationId(),
                        payment.getId(),
                        payment.getStatus().name(),
                        event.getPayload().getCustomerEmail()
                )
        );

        kafkaTemplate.send(
                TOPIC,
                paymentCompletedEvent.getPayload().getReservationId().toString(),
                paymentCompletedEvent
        );
    }
}