package com.medicalstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Maps a subscription plan tier (FREE, PRO, ENTERPRISE) to a feature flag code.
 * Templates consume these via the {@code availableFeatures} Set exposed by
 * {@link com.medicalstore.config.GlobalModelAttribute}.
 *
 * <p>Known feature codes:
 * <ul>
 *   <li>INVOICE_PRINT — print / download PDF invoices</li>
 *   <li>BASIC_REPORTS — standard sales/stock summary reports</li>
 *   <li>ADVANCED_ANALYTICS — profit, dead-stock, fast-moving charts</li>
 *   <li>EXCEL_EXPORT — export analytics to Excel</li>
 *   <li>BULK_EXPORT — bulk data export across all branches</li>
 *   <li>API_ACCESS — REST API key access (future)</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscription_features",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_sub_feature",
                columnNames = {"plan_type", "feature_code"}),
        indexes = @Index(name = "idx_sub_feat_plan", columnList = "plan_type"))
public class SubscriptionFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Plan tier: FREE, PRO, ENTERPRISE */
    @Column(name = "plan_type", nullable = false, length = 30)
    private String planType;

    /** Feature flag code (e.g. EXCEL_EXPORT) */
    @Column(name = "feature_code", nullable = false, length = 100)
    private String featureCode;
}
