package com.medicalstore.repository;

import com.medicalstore.model.SubscriptionFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface SubscriptionFeatureRepository extends JpaRepository<SubscriptionFeature, Long> {

    /**
     * Returns all feature codes enabled for the given plan tier.
     * Used by {@link com.medicalstore.service.SubscriptionService} for plan-based caching.
     */
    @Query("SELECT f.featureCode FROM SubscriptionFeature f WHERE f.planType = :planType")
    Set<String> findFeatureCodesByPlanType(@Param("planType") String planType);

    boolean existsByPlanTypeAndFeatureCode(String planType, String featureCode);
}
