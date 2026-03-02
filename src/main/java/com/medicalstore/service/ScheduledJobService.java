package com.medicalstore.service;

import com.medicalstore.model.SubscriptionPlan;
import com.medicalstore.repository.MedicineRepository;
import com.medicalstore.repository.SubscriptionPlanRepository;
import com.medicalstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled background jobs for housekeeping tasks:
 * <ul>
 *   <li>Daily expiry-alert logging for medicines nearing their expiry date</li>
 *   <li>Daily subscription expiry enforcement (mark overdue plans inactive)</li>
 *   <li>Weekly purge of soft-deleted users older than the configured retention period</li>
 * </ul>
 *
 * Requires {@code @EnableScheduling} on the application class.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledJobService {

    /** How many days ahead to flag medicines as "expiring soon". */
    @Value("${scheduler.expiry-alert-days:30}")
    private int expiryAlertDays;

    /** How many days a soft-deleted user record is kept before hard-deletion. */
    @Value("${scheduler.soft-delete-retention-days:90}")
    private int retentionDays;

    private final MedicineRepository medicineRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // Job 1 — Daily expiry-alert check  (every day at 06:00)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Logs medicines that will expire within the next {@code expiryAlertDays} days.
     * Does NOT mutate any data — purely informational so that an operator or
     * monitoring system can detect the log lines and trigger alerts.
     *
     * <p>Cron: {@code 0 0 6 * * *} → runs once per day at 06:00 server time.
     */
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional(readOnly = true)
    public void refreshExpiryAlerts() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(expiryAlertDays);

        List<?> expiring = medicineRepository.findByExpiryDateBetween(today, threshold);
        if (expiring.isEmpty()) {
            log.info("[SCHEDULER] expiry-alert: no medicines expiring within {} days", expiryAlertDays);
            return;
        }

        log.warn("[SCHEDULER] expiry-alert: {} medicine batch(es) expiring between {} and {} (within {} days) — review inventory",
                expiring.size(), today, threshold, expiryAlertDays);

        // Detailed per-entry log so operators can grep by name/batch
        expiring.forEach(m -> {
            if (m instanceof com.medicalstore.model.Medicine med) {
                log.warn("[SCHEDULER] expiry-alert  id={} name='{}' batch='{}' qty={} expiryDate={}",
                        med.getId(), med.getName(), med.getBatchNumber(),
                        med.getQuantity(), med.getExpiryDate());
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Job 2 — Daily subscription expiry enforcement  (every day at 01:00)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Marks subscription plans whose expiry date has passed as inactive.
     * Evicts the {@code subscription_plan} cache so the next request forces a
     * fresh DB read and the user sees the expired state immediately.
     *
     * <p>Cron: {@code 0 0 1 * * *} → runs once per day at 01:00 server time.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    @CacheEvict(value = "subscription_plan", allEntries = true)
    public void checkSubscriptionExpiry() {
        LocalDate today = LocalDate.now();

        List<SubscriptionPlan> expired = subscriptionPlanRepository
                .findByExpiryDateBeforeAndActiveTrue(today);

        if (expired.isEmpty()) {
            log.info("[SCHEDULER] subscription-expiry: all plans are current as of {}", today);
            return;
        }

        expired.forEach(plan -> {
            plan.setActive(false);
            Long ownerId = plan.getOwner() != null ? plan.getOwner().getId() : null;
            log.warn("[SCHEDULER] subscription-expiry: deactivated planId={} ownerId={} planType={} expiredOn={}",
                    plan.getId(), ownerId, plan.getPlanType(), plan.getExpiryDate());
        });

        subscriptionPlanRepository.saveAll(expired);
        log.info("[SCHEDULER] subscription-expiry: {} plan(s) deactivated on {}", expired.size(), today);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Job 3 — Weekly soft-delete purge  (every Sunday at 03:00)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Permanently hard-deletes user records that were soft-deleted more than
     * {@code retentionDays} days ago.  This keeps the {@code users} table lean
     * and ensures compliance with data-retention policies.
     *
     * <p>Only the {@code users} table is purged here because other soft-deleted
     * entities (e.g. {@code medicines}) do not record a {@code deleted_at}
     * timestamp and therefore cannot be age-filtered safely.
     *
     * <p>Cron: {@code 0 0 3 * * 0} → runs every Sunday at 03:00 server time.
     */
    @Scheduled(cron = "0 0 3 * * 0")
    @Transactional
    public void purgeOldSoftDeletes() {
        int deleted = userRepository.purgeOldSoftDeleted(retentionDays);
        if (deleted == 0) {
            log.info("[SCHEDULER] soft-delete-purge: no user records older than {} days found", retentionDays);
        } else {
            log.info("[SCHEDULER] soft-delete-purge: hard-deleted {} user record(s) with deleted_at older than {} days",
                    deleted, retentionDays);
        }
    }
}
