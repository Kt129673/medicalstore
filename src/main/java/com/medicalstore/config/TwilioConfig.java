package com.medicalstore.config;

import com.twilio.Twilio;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Slf4j
@Configuration
public class TwilioConfig {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.enabled:false}")
    private boolean whatsappEnabled;

    @PostConstruct
    public void initTwilio() {
        // Validate credentials are not placeholders
        if ("YOUR_ACCOUNT_SID".equals(accountSid) || "YOUR_AUTH_TOKEN".equals(authToken)) {
            log.warn("WhatsApp integration disabled: Twilio credentials are placeholders. Set TWILIO_ACCOUNT_SID and TWILIO_AUTH_TOKEN environment variables to enable.");
            return;
        }
        
        if (whatsappEnabled) {
            try {
                Twilio.init(accountSid, authToken);
                log.info("Twilio WhatsApp initialized successfully!");
            } catch (Exception e) {
                log.error("Failed to initialize Twilio WhatsApp: {}", e.getMessage());
                throw new IllegalStateException("Twilio initialization failed. Check credentials.", e);
            }
        } else {
            log.info("WhatsApp integration disabled via configuration (twilio.whatsapp.enabled=false)");
        }
    }
}
