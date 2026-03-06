package com.medicalstore.kafka;

/**
 * Centralized Kafka topic names and consumer group IDs.
 *
 * <p>
 * All topic names follow the convention {@code medicalstore.<domain>.events}.
 * Keeping them in a single class avoids hard-coded strings scattered across
 * producers and consumers.
 * </p>
 */
public final class KafkaConstants {

    private KafkaConstants() {
        // utility class — prevent instantiation
    }

    // ─── Topic Names ──────────────────────────────────────────────────────────
    /** Sale created, deleted, or returned. */
    public static final String TOPIC_SALES = "medicalstore.sales.events";

    /** Medicine CRUD and stock-level changes. */
    public static final String TOPIC_INVENTORY = "medicalstore.inventory.events";

    /** User/role CRUD and security-related changes. */
    public static final String TOPIC_AUDIT = "medicalstore.audit.events";

    /** WhatsApp alerts — expiry, low-stock, invoice. */
    public static final String TOPIC_NOTIFICATIONS = "medicalstore.notification.events";

    /** Purchase order created, received, or cancelled. */
    public static final String TOPIC_PURCHASES = "medicalstore.purchase.events";

    // ─── Consumer Group IDs ───────────────────────────────────────────────────
    /** Group for the notification consumer (WhatsApp). */
    public static final String GROUP_NOTIFICATION = "medicalstore-notification-group";

    /** Group for the audit-log consumer (DB persistence). */
    public static final String GROUP_AUDIT = "medicalstore-audit-group";

    /** Group for the inventory-alert consumer (low-stock / expiry). */
    public static final String GROUP_INVENTORY_ALERT = "medicalstore-inventory-alert-group";
}
