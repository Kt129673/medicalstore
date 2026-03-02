package com.medicalstore.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine-backed local cache configuration.
 *
 * <p>TTL policy:
 * <ul>
 *   <li>dashboard_kpis / dashboard_charts — <strong>30 seconds</strong> (high write frequency)</li>
 *   <li>All analytic, search, subscription, permission caches — <strong>60 seconds</strong></li>
 * </ul>
 *
 * <p>Cache invalidations on write operations are managed via {@code @CacheEvict} /
 * {@code @Caching} annotations on the relevant service write methods — no Redis needed.</p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final int COMMON_MAX = 5_000;
    private static final int INITIAL     = 100;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                // ── 30-second dashboard caches (refresh fast KPIs)
                build("dashboard_kpis",       30),
                build("dashboard_charts",     30),

                // ── 60-second caches
                build("medicines_search",     60),
                build("subscription_plan",    60),
                build("analytics_profit",     60),
                build("analytics_deadstock",  60),
                build("analytics_fastmoving", 60),
                build("analytics_gst",        60),
                build("role_permissions",     60),
                build("plan_features",        60)
        ));
        return manager;
    }

    private CaffeineCache build(String name, int ttlSeconds) {
        return new CaffeineCache(name,
                Caffeine.newBuilder()
                        .initialCapacity(INITIAL)
                        .maximumSize(COMMON_MAX)
                        .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
                        .recordStats()
                        .build());
    }
}
