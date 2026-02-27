package com.medicalstore.repository;

import com.medicalstore.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByOwnerId(Long ownerId);

    List<SubscriptionPlan> findByExpiryDateBeforeAndActiveTrue(LocalDate date);
}
