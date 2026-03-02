package com.medicalstore.util;

/**
 * @deprecated Moved to {@link com.medicalstore.common.SecurityUtils}.
 *             This file is intentionally left as a non-functional stub so that
 *             any missed import still produces a clear compile error pointing at
 *             the right class rather than a silent NoSuchBeanDefinitionException.
 *
 * <p><b>Do NOT annotate this class with {@code @Component}</b> — doing so would
 * register a second bean, breaking dependency injection for
 * {@link com.medicalstore.common.SecurityUtils}.
 *
 * @see com.medicalstore.common.SecurityUtils
 */
@Deprecated(since = "2.0", forRemoval = true)
public final class SecurityUtils {

    private SecurityUtils() {
        throw new UnsupportedOperationException(
                "Use com.medicalstore.common.SecurityUtils (Spring bean)");
    }
}
