package com.hotel.notificationservice.config;

import com.hotel.notificationservice.event.PaymentCompletedEvent;
import com.hotel.notificationservice.event.ReservationCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    private Map<String, Object> baseConsumerConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }

    @Bean
    public ConsumerFactory<String, ReservationCreatedEvent> reservationConsumerFactory() {
        JsonDeserializer<ReservationCreatedEvent> deserializer =
            new JsonDeserializer<>(ReservationCreatedEvent.class);
        deserializer.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(
            baseConsumerConfig(),
            new StringDeserializer(),
            deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ReservationCreatedEvent>
            reservationKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ReservationCreatedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(reservationConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, PaymentCompletedEvent> paymentConsumerFactory() {
        JsonDeserializer<PaymentCompletedEvent> deserializer =
            new JsonDeserializer<>(PaymentCompletedEvent.class);
        deserializer.setUseTypeHeaders(false);
        return new DefaultKafkaConsumerFactory<>(
            baseConsumerConfig(),
            new StringDeserializer(),
            deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent>
            paymentKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentConsumerFactory());
        return factory;
    }
}