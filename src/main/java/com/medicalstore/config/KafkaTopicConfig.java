package com.medicalstore.config;

import com.medicalstore.kafka.KafkaConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Auto-creates Kafka topics on application startup.
 *
 * <p>
 * Only active when {@code kafka.enabled=true}. Each topic is created with
 * a sensible default of 3 partitions and 1 replica (suitable for local dev;
 * increase the replica factor for production clusters).
 * </p>
 */
@Configuration
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaTopicConfig {

    private static final int PARTITIONS = 3;
    private static final int REPLICAS = 1;

    /** Sale created / deleted / returned. */
    @Bean
    public NewTopic salesEventsTopic() {
        return TopicBuilder.name(KafkaConstants.TOPIC_SALES)
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .build();
    }

    /** Medicine CRUD and stock changes. */
    @Bean
    public NewTopic inventoryEventsTopic() {
        return TopicBuilder.name(KafkaConstants.TOPIC_INVENTORY)
                .partitions(PARTITIONS)
                .replicas(REPLICAS)
                .build();
    }

    /** User / role audit trail entries. */
    @Bean
    public NewTopic auditEventsTopic() {
        return TopicBuilder.name(KafkaConstants.TOPIC_AUDIT)
                .partitions(2)
                .replicas(REPLICAS)
                .build();
    }

    /** WhatsApp and other notification alerts. */
    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name(KafkaConstants.TOPIC_NOTIFICATIONS)
                .partitions(2)
                .replicas(REPLICAS)
                .build();
    }

    /** Purchase order lifecycle events. */
    @Bean
    public NewTopic purchaseEventsTopic() {
        return TopicBuilder.name(KafkaConstants.TOPIC_PURCHASES)
                .partitions(2)
                .replicas(REPLICAS)
                .build();
    }
}
