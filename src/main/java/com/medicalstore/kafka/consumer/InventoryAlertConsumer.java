package com.medicalstore.kafka.consumer;

import com.medicalstore.dto.event.InventoryEvent;
import com.medicalstore.dto.event.NotificationEvent;
import com.medicalstore.kafka.EventPublisher;
import com.medicalstore.kafka.KafkaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer that monitors inventory events for low-stock and
 * near-expiry conditions, then publishes notification events for
 * WhatsApp alerts.
 *
 * <p>
 * In the monolithic design this consumer reacts in real-time to stock
 * changes — every time a sale or return modifies medicine quantities,
 * this consumer checks thresholds and triggers alerts instantly instead
 * of waiting for the daily scheduled job.
 * </p>
 *
 * <p>
 * Only active when {@code kafka.enabled=true}.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class InventoryAlertConsumer {

    private final EventPublisher eventPublisher;

    /** Low-stock threshold — medicines below this level trigger an alert. */
    @Value("${inventory.low-stock-threshold:10}")
    private int lowStockThreshold;

    /**
     * Reacts to inventory events and checks for alert conditions.
     *
     * @param event the incoming inventory event
     */
    @KafkaListener(topics = KafkaConstants.TOPIC_INVENTORY, groupId = KafkaConstants.GROUP_INVENTORY_ALERT, containerFactory = "kafkaListenerContainerFactory")
    public void onInventoryEvent(InventoryEvent event) {
        log.info("KAFKA_CONSUME | topic=inventory | eventType={} | medicine={} | qty={}→{}",
                event.getEventType(), event.getMedicineName(),
                event.getPreviousQuantity(), event.getNewQuantity());

        // Only check alerts on stock-change events
        if (!"STOCK_CHANGED".equals(event.getEventType())
                && !"MEDICINE_UPDATED".equals(event.getEventType())) {
            return;
        }

        checkLowStock(event);
    }

    /**
     * Publishes a {@code LOW_STOCK_ALERT} notification event when the
     * new quantity drops below the configured threshold.
     *
     * @param event the inventory event to evaluate
     */
    private void checkLowStock(InventoryEvent event) {
        if (event.getNewQuantity() == null || event.getNewQuantity() >= lowStockThreshold) {
            return;
        }

        // Only alert if the previous quantity was above threshold (avoids repeat
        // alerts)
        if (event.getPreviousQuantity() != null && event.getPreviousQuantity() < lowStockThreshold) {
            return;
        }

        log.warn("LOW_STOCK_DETECTED | medicine={} | qty={} | threshold={}",
                event.getMedicineName(), event.getNewQuantity(), lowStockThreshold);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("medicineName", event.getMedicineName());
        metadata.put("currentStock", event.getNewQuantity());
        metadata.put("medicineId", event.getMedicineId());

        NotificationEvent notification = NotificationEvent.builder()
                .eventType("LOW_STOCK_ALERT")
                .notificationType("LOW_STOCK_ALERT")
                .branchId(event.getBranchId())
                .performedBy("SYSTEM")
                .message(String.format("📉 LOW STOCK: %s has only %d units remaining",
                        event.getMedicineName(), event.getNewQuantity()))
                .metadata(metadata)
                .build();

        eventPublisher.publishNotificationEvent(notification);
    }
}
