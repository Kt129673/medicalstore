package com.medicalstore.service;

import com.medicalstore.model.SubscriptionPlan;
import com.medicalstore.model.User;
import com.medicalstore.repository.SubscriptionPlanRepository;
import com.medicalstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    private final UserRepository userRepository;
    private final com.medicalstore.util.SecurityUtils securityUtils;

    @Cacheable(value = "subscription_plan", key = "#ownerId")
    public Optional<SubscriptionPlan> getPlanForOwner(Long ownerId) {
        return planRepository.findByOwnerId(ownerId);
    }

    @Transactional
    @CacheEvict(value = "subscription_plan", key = "#ownerId")
    public SubscriptionPlan createOrUpdatePlan(Long ownerId, String planType, LocalDate expiryDate, int maxUsers,
            int maxBranches) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        SubscriptionPlan plan = planRepository.findByOwnerId(ownerId)
                .orElse(new SubscriptionPlan());

        plan.setOwner(owner);
        plan.setPlanType(planType);
        plan.setExpiryDate(expiryDate);
        plan.setMaxUsers(maxUsers);
        plan.setMaxBranches(maxBranches);
        plan.setActive(true);

        return planRepository.save(plan);
    }

    public boolean isSubscriptionActive(Long ownerId) {
        return planRepository.findByOwnerId(ownerId)
                .map(p -> p.isActive() && !p.isExpired())
                .orElse(false); // If no plan exists, assume blocked or default to trial. For now: false.
    }

    public SubscriptionPlan getEffectivePlan() {
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();
        if (ownerId == null && com.medicalstore.config.TenantContext.getTenantId() != null) {
            ownerId = securityUtils.getCurrentOwnerId();
        }
        if (ownerId != null) {
            return getPlanForOwner(ownerId).orElse(null);
        }
        return null;
    }
}
