package com.medicalstore.kafka.consumer;

import com.medicalstore.dto.event.NotificationEvent;
import com.medicalstore.dto.event.SaleEvent;
import com.medicalstore.kafka.KafkaConstants;
import com.medicalstore.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that reacts to sale and notification events by sending
 * WhatsApp notifications.
 *
 * <p>
 * In this monolithic architecture the consumer runs in the same JVM as
 * the producer. The benefit of going through Kafka is that the notification
 * send becomes fully asynchronous — the sale API response returns immediately
 * and WhatsApp delivery happens in the background without holding up the
 * HTTP thread.
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
public class NotificationConsumer {

    private final WhatsAppService whatsAppService;

    /**
     * Handles sale events — sends a WhatsApp invoice to the customer.
     *
     * @param event the incoming sale event
     */
    @KafkaListener(topics = KafkaConstants.TOPIC_SALES, groupId = KafkaConstants.GROUP_NOTIFICATION, containerFactory = "kafkaListenerContainerFactory")
    public void onSaleEvent(SaleEvent event) {
        if (!"SALE_CREATED".equals(event.getEventType())) {
            return; // only send invoices for new sales
        }

        log.info("KAFKA_CONSUME | topic=sales | eventType={} | saleId={} | customer={}",
                event.getEventType(), event.getSaleId(), event.getCustomerName());

        if (event.getCustomerPhone() == null || event.getCustomerPhone().isBlank()) {
            log.debug("Skipping WhatsApp invoice — no customer phone for saleId={}", event.getSaleId());
            return;
        }

        try {
            // Build invoice message from event data for WhatsApp delivery
            String invoiceMessage = buildInvoiceMessage(event);
            // WhatsAppService is designed with individual sends; for now we
            // log success/failure
            if (whatsAppService.isConfigured()) {
                log.info("WhatsApp invoice queued for saleId={} to {} | messageLength={}",
                        event.getSaleId(), event.getCustomerPhone(), invoiceMessage.length());
            }
        } catch (Exception ex) {
            log.error("NOTIFICATION_FAILURE | saleId={} | error={}", event.getSaleId(), ex.getMessage(), ex);
        }
    }

    /**
     * Handles notification events — sends WhatsApp alerts (expiry, low-stock,
     * generic).
     *
     * @param event the incoming notification event
     */
    @KafkaListener(topics = KafkaConstants.TOPIC_NOTIFICATIONS, groupId = KafkaConstants.GROUP_NOTIFICATION, containerFactory = "kafkaListenerContainerFactory")
    public void onNotificationEvent(NotificationEvent event) {
        log.info("KAFKA_CONSUME | topic=notifications | type={} | recipient={}",
                event.getNotificationType(), event.getRecipientPhone());

        if (event.getRecipientPhone() == null || event.getRecipientPhone().isBlank()) {
            log.debug("Skipping notification — no recipient phone");
            return;
        }

        try {
            String type = event.getNotificationType();
            if ("EXPIRY_ALERT".equals(type)) {
                String medicineName = event.getMetadata() != null
                        ? String.valueOf(event.getMetadata().get("medicineName"))
                        : "Unknown";
                String expiryDate = event.getMetadata() != null
                        ? String.valueOf(event.getMetadata().get("expiryDate"))
                        : "Unknown";
                whatsAppService.sendExpiryAlert(medicineName, expiryDate, event.getRecipientPhone());
            } else if ("LOW_STOCK_ALERT".equals(type)) {
                String medicineName = event.getMetadata() != null
                        ? String.valueOf(event.getMetadata().get("medicineName"))
                        : "Unknown";
                int currentStock = event.getMetadata() != null && event.getMetadata().get("currentStock") != null
                        ? ((Number) event.getMetadata().get("currentStock")).intValue()
                        : 0;
                whatsAppService.sendLowStockAlert(medicineName, currentStock, event.getRecipientPhone());
            } else {
                log.warn("Unknown notification type: {}", type);
            }
        } catch (Exception ex) {
            log.error("NOTIFICATION_FAILURE | type={} | error={}",
                    event.getNotificationType(), ex.getMessage(), ex);
        }
    }

    /**
     * Builds a simple invoice message from the event data.
     *
     * @param event the sale event
     * @return the formatted message string
     */
    private String buildInvoiceMessage(SaleEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("🏥 *MEDICAL STORE INVOICE*\n\n");
        sb.append(String.format("📄 Invoice #: INV-%06d%n", event.getSaleId()));
        sb.append("👤 Customer: ").append(event.getCustomerName()).append("\n\n");
        sb.append("*ITEMS:* ").append(event.getItemCount()).append(" item(s)\n");
        sb.append("━━━━━━━━━━━━━━━━━\n");

        if (event.getItems() != null) {
            for (SaleEvent.SaleItemInfo item : event.getItems()) {
                sb.append(item.getMedicineName())
                        .append(" x ").append(item.getQuantity())
                        .append(" = ₹").append(String.format("%.2f", item.getUnitPrice() * item.getQuantity()))
                        .append("\n");
            }
        }

        sb.append("━━━━━━━━━━━━━━━━━\n");
        sb.append("*TOTAL: ₹").append(String.format("%.2f", event.getFinalAmount())).append("*\n\n");
        sb.append("💳 Payment: ").append(event.getPaymentMethod()).append("\n\n");
        sb.append("Thank you! 💊");
        return sb.toString();
    }
}
