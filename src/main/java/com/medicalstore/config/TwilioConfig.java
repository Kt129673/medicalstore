package com.medicalstore.config;

import com.twilio.Twilio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

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
        if (whatsappEnabled && !accountSid.equals("YOUR_ACCOUNT_SID")) {
            Twilio.init(accountSid, authToken);
            System.out.println("✅ Twilio WhatsApp initialized successfully!");
        } else {
            System.out.println("⚠️ WhatsApp integration disabled. Configure Twilio credentials to enable.");
        }
    }
}
