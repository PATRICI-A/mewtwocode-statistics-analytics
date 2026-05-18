package edu.eci.patriciaM12.infrastructure.config;

import edu.eci.patriciaM12.infrastructure.adapters.messaging.dto.InboundPatchEventMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configures the Kafka consumer infrastructure for M12 (Statistics & Analytics).
 * <p>
 * Enables {@code @KafkaListener} processing via {@link EnableKafka} and registers a
 * {@link ConcurrentKafkaListenerContainerFactory} that deserializes message values as
 * {@link InboundPatchEventMessage} JSON objects.
 * </p>
 *
 * <p>Consumer group: {@code m12-analytics-group} (defined in {@code application.properties}).<br>
 * Topic subscribed: {@code m06-patch-events} (published by M06 Feed & Search).</p>
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Creates a {@link ConsumerFactory} that deserializes Kafka message values as
     * {@link InboundPatchEventMessage} JSON objects.
     * All packages are trusted for deserialization since M06 and M12 share known event types.
     *
     * @return the configured {@link ConsumerFactory}
     */
    @Bean
    public ConsumerFactory<String, InboundPatchEventMessage> patchEventConsumerFactory() {
        JsonDeserializer<InboundPatchEventMessage> deserializer =
                new JsonDeserializer<>(InboundPatchEventMessage.class, false);
        deserializer.addTrustedPackages("*");

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    /**
     * Creates the {@link ConcurrentKafkaListenerContainerFactory} used by all
     * {@code @KafkaListener} methods in M12 that consume {@link InboundPatchEventMessage}.
     * Concurrency is set to 1 to avoid out-of-order processing of per-patch events.
     *
     * @return the configured {@link ConcurrentKafkaListenerContainerFactory}
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InboundPatchEventMessage>
    patchEventListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, InboundPatchEventMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(patchEventConsumerFactory());
        factory.setConcurrency(1);
        return factory;
    }
}
