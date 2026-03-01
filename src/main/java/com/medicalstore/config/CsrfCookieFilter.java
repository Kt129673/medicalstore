package com.medicalstore.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Force-resolves the deferred CSRF token early in the request lifecycle,
 * <em>before</em> any response bytes are written.
 * <p>
 * Spring Security 6.x defers CSRF token generation (lazy Supplier). The token
 * is only materialised when something calls {@code CsrfToken.getToken()}.
 * If the Thymeleaf template is large enough to flush Tomcat's 8 KB response
 * buffer before the form's {@code th:action} triggers token resolution,
 * the response is already committed and Tomcat cannot set the session cookie.
 * This produces:
 * <pre>
 *   IllegalStateException: Cannot create a session after the response has been committed
 * </pre>
 * By eagerly calling {@code getToken()} here the session (and its cookie) are
 * created while the response is still pristine.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            // Force the deferred token to materialise (creates session + cookie)
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
