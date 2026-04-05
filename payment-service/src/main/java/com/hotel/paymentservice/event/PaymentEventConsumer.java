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

//making sure its managed by spring and can be injected where needed
//it is a bit different from bean annotation as it automatically creates 
// in bean we have to tell spring to create an instance of the class and 
// manage its lifecycle but in component we just have to 
// annotate the class and spring will automatically create
//  an instance of it and manage its lifecycle
@Component
public class PaymentEventConsumer {

     private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);
 
    PaymentRepository paymentRepository;
    ProcessedEventRepository processedEventRepository; 
    private final KafkaTemplate<String,PaymentCompletedEvent> kafkaTemplate;
    
    private static final String TOPIC="payment-events";   
    private int failureCount=0;

    public PaymentEventConsumer(PaymentRepository paymentRepository, ProcessedEventRepository processedEventRepository, KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.processedEventRepository = processedEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void setFailureCounter(int failureCount) {
        this.failureCount = failureCount;
    }
    @KafkaListener(topics= "reservation-events", groupId="payment-service-group")
    public void handleRegistrationCreatedEvent(ReservationCreatedEvent event)
    {
        log.info("Received event in payment. Email: {}", event.getPayload().getCustomerEmail());

        if (event == null || event.getPayload() == null || event.getPayload().getReservationId() == null) {
    log.error("Invalid event received, skipping processing. Event: {}", event);
    return;
}
        //lets first check if this event is already presnet in the table 
        //for idempotency checks
        if(processedEventRepository.existsByEventId(event.getEventId()))
        {
          log.warn("Duplicate event detected, discarding: {}", event.getEventId());
          return;
        }

        Payment payment=new Payment();
        payment.setReservationId(event.getPayload().getReservationId());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        Payment savedPayment= paymentRepository.save(payment);
        log.info("Payment created successfully: {}", savedPayment.getId());

        //lets now simulate randomly success or failure
        boolean PaymentSuccess= Math.random()>0.3;
        log.info("Payment simulation result: {}", PaymentSuccess ? "SUCCESS" : "FAILED");
        savedPayment.setStatus(PaymentSuccess?Payment.PaymentStatus.SUCCESS:Payment.PaymentStatus.FAILED);
        paymentRepository.save(savedPayment);

         //ab publish karoww
         PaymentCompletedEvent paymentCompletedEvent= new PaymentCompletedEvent(
           UUID.randomUUID().toString(),
           "PAYMENT_COMPLETED",
           LocalDateTime.now(), 
           new PaymentCompletedEvent.Payload(event.getPayload().getReservationId(),
            savedPayment.getId(), savedPayment.getStatus().name(),event.getPayload().getCustomerEmail())
         );
         kafkaTemplate.send(TOPIC,
             paymentCompletedEvent.getPayload().getReservationId().toString(), paymentCompletedEvent);
        

         processedEventRepository.save(
            new ProcessedEvent(event.getEventId(),LocalDateTime.now())
         );

          log.info("PaymentCompletedEvent published for reservationId: {}", event.getPayload().getReservationId());
    }

  
}
