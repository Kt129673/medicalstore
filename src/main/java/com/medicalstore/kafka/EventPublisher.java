package com.medicalstore.kafka;

import com.medicalstore.dto.event.AuditEvent;
import com.medicalstore.dto.event.BaseEvent;
import com.medicalstore.dto.event.InventoryEvent;
import com.medicalstore.dto.event.NotificationEvent;
import com.medicalstore.dto.event.PurchaseEvent;
import com.medicalstore.dto.event.SaleEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Central gateway for publishing domain events to Kafka.
 *
 * <p>
 * All service classes inject this component instead of interacting with
 * {@link KafkaTemplate} directly. When {@code kafka.enabled} is {@code false}
 * (or the {@code KafkaTemplate} bean is absent), every publish method
 * silently no-ops — ensuring the application works without a Kafka broker.
 * </p>
 *
 * <p>
 * Keys are always {@code String} (typically the branchId) so events for
 * the same branch end up on the same partition.
 * </p>
 */
@Slf4j
@Component
public class EventPublisher {

    /** Nullable — only present when {@code kafka.enabled=true}. */
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final boolean kafkaEnabled;

    /**
     * Constructor — the {@code KafkaTemplate} parameter is optional.
     * If Kafka is disabled the template will be {@code null} and all
     * publish calls become no-ops.
     *
     * @param kafkaTemplate the Kafka template (may be {@code null})
     * @param kafkaEnabled  the {@code kafka.enabled} property value
     */
    public EventPublisher(
            @Autowired(required = false) KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${kafka.enabled:false}") boolean kafkaEnabled) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaEnabled = kafkaEnabled;
    }

    // ─── Topic-specific publish methods ───────────────────────────────────────

    /**
     * Publishes a sale event to the sales topic.
     *
     * @param event the sale event payload
     */
    public void publishSaleEvent(SaleEvent event) {
        publish(KafkaConstants.TOPIC_SALES, event);
    }

    /**
     * Publishes an inventory event to the inventory topic.
     *
     * @param event the inventory event payload
     */
    public void publishInventoryEvent(InventoryEvent event) {
        publish(KafkaConstants.TOPIC_INVENTORY, event);
    }

    /**
     * Publishes an audit event to the audit topic.
     *
     * @param event the audit event payload
     */
    public void publishAuditEvent(AuditEvent event) {
        publish(KafkaConstants.TOPIC_AUDIT, event);
    }

    /**
     * Publishes a notification event to the notification topic.
     *
     * @param event the notification event payload
     */
    public void publishNotificationEvent(NotificationEvent event) {
        publish(KafkaConstants.TOPIC_NOTIFICATIONS, event);
    }

    /**
     * Publishes a purchase event to the purchase topic.
     *
     * @param event the purchase event payload
     */
    public void publishPurchaseEvent(PurchaseEvent event) {
        publish(KafkaConstants.TOPIC_PURCHASES, event);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    /**
     * Low-level send that guards against a disabled Kafka environment.
     *
     * @param topic the target Kafka topic
     * @param event the event payload
     */
    private void publish(String topic, BaseEvent event) {
        if (!kafkaEnabled || kafkaTemplate == null) {
            log.debug("Kafka disabled — skipping event: {} → {}", topic, event.getEventType());
            return;
        }

        event.initDefaults();
        String key = event.getBranchId() != null ? String.valueOf(event.getBranchId()) : "global";

        try {
            kafkaTemplate.send(topic, key, event);
            log.info("KAFKA_PUBLISH | topic={} | key={} | eventType={} | eventId={}",
                    topic, key, event.getEventType(), event.getEventId());
        } catch (Exception ex) {
            // Publishing failure must never break the main business flow
            log.error("KAFKA_PUBLISH_FAILURE | topic={} | eventType={} | error={}",
                    topic, event.getEventType(), ex.getMessage(), ex);
        }
    }
}
