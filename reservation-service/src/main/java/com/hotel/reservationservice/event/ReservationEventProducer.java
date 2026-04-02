package com.hotel.reservationservice.event;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReservationEventProducer {
 
    private static final String TOPIC="reservation-events";
    private final KafkaTemplate<String,ReservationCreatedEvent>kafkaTemplate;

    public ReservationEventProducer(KafkaTemplate<String, ReservationCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishReservationCreatedEvent(ReservationCreatedEvent event)
    {
        //the format has topic,key,value here in this case 
        kafkaTemplate.send(TOPIC,event.getPayload().getReservationId().toString(),event);
    }
}
