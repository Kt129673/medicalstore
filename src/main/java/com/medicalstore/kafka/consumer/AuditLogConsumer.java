package com.medicalstore.kafka.consumer;

import com.medicalstore.dto.event.AuditEvent;
import com.medicalstore.dto.event.PurchaseEvent;
import com.medicalstore.dto.event.SaleEvent;
import com.medicalstore.kafka.KafkaConstants;
import com.medicalstore.model.AuditLog;
import com.medicalstore.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that persists audit records from multiple event topics.
 *
 * <p>
 * This consumer subscribes to sales, audit, inventory, and purchase topics
 * and writes a standard {@link AuditLog} record for each event. In the
 * monolithic architecture it runs alongside the services in the same JVM,
 * but because it goes through Kafka the audit write is fully decoupled from
 * the business transaction — a slow DB insert never blocks the main API
 * response, and events are retried automatically on failure.
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
public class AuditLogConsumer {

    private final AuditLogRepository auditLogRepository;

    /**
     * Persists an audit record for dedicated audit events (user/role changes).
     *
     * @param event the incoming audit event
     */
    @KafkaListener(topics = KafkaConstants.TOPIC_AUDIT, groupId = KafkaConstants.GROUP_AUDIT, containerFactory = "kafkaListenerContainerFactory")
    public void onAuditEvent(AuditEvent event) {
        log.info("KAFKA_CONSUME | topic=audit | action={} | entity={} | performer={}",
                event.getAction(), event.getEntityName(), event.getPerformedBy());

        persistAuditLog(
                event.getEntityName(),
                event.getEntityId(),
                event.getAction(),
                event.getDescription(),
                event.getPerformedBy(),
                event.getPerformedByRole(),
                event.getOldValue(),
                event.getNewValue(),
                event.getIpAddress());
    }

    /**
     * Persists an audit record for sale events.
     *
     * @param event the incoming sale event
     */
    @KafkaListener(topics = KafkaConstants.TOPIC_SALES, groupId = KafkaConstants.GROUP_AUDIT, containerFactory = "kafkaListenerContainerFactory")
    public void onSaleEvent(SaleEvent event) {
        log.info("KAFKA_CONSUME | topic=sales (audit) | eventType={} | saleId={}",
                event.getEventType(), event.getSaleId());

        persistAuditLog(
                "Sale",
                event.getSaleId(),
                event.getEventType(),
                String.format("Sale %s | amount=₹%.2f | items=%d",
                        event.getEventType(), event.getFinalAmount(), event.getItemCount()),
                event.getPerformedBy(),
                null, null, null, null);
    }

    /**
     * Persists an audit record for purchase events.
     *
     * @param event the incoming purchase event
     */
    @KafkaListener(topics = KafkaConstants.TOPIC_PURCHASES, groupId = KafkaConstants.GROUP_AUDIT, containerFactory = "kafkaListenerContainerFactory")
    public void onPurchaseEvent(PurchaseEvent event) {
        log.info("KAFKA_CONSUME | topic=purchases (audit) | eventType={} | orderId={}",
                event.getEventType(), event.getPurchaseOrderId());

        persistAuditLog(
                "PurchaseOrder",
                event.getPurchaseOrderId(),
                event.getEventType(),
                String.format("PO %s | order=%s | amount=₹%.2f",
                        event.getEventType(), event.getOrderNumber(), event.getTotalAmount()),
                event.getPerformedBy(),
                null, null, null, null);
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    /**
     * Creates and saves an {@link AuditLog} record.
     */
    private void persistAuditLog(String entityName, Long entityId, String action,
            String description, String performedBy, String performedByRole,
            String oldValue, String newValue, String ipAddress) {
        try {
            AuditLog record = AuditLog.builder()
                    .entityName(entityName)
                    .entityId(entityId)
                    .action(action)
                    .description(description)
                    .performedBy(performedBy)
                    .performedByRole(performedByRole)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .build();
            auditLogRepository.save(record);
        } catch (Exception ex) {
            log.error("AUDIT_PERSIST_FAILURE | action={} | performer={} | error={}",
                    action, performedBy, ex.getMessage(), ex);
        }
    }
}
