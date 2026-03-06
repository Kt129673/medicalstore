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
        if (whatsappEnabled && !"YOUR_ACCOUNT_SID".equals(accountSid)) {
            Twilio.init(accountSid, authToken);
            log.info("Twilio WhatsApp initialized successfully!");
        } else {
            log.warn("WhatsApp integration disabled. Configure Twilio credentials to enable.");
        }
    }
}
