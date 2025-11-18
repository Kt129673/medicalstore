# WhatsApp Integration Setup Guide

## Overview
The Medical Store application now supports sending invoices and alerts via WhatsApp using Twilio's WhatsApp Business API.

## Features
1. **Send Invoices** - Automatically send formatted invoices to customers via WhatsApp
2. **Expiry Alerts** - Send medicine expiry alerts to admin/staff
3. **Low Stock Alerts** - Notify admin when stock is running low

## Prerequisites
- Twilio Account (Free trial available)
- WhatsApp-enabled phone number from Twilio
- Customer phone numbers in E.164 format (+91XXXXXXXXXX)

## Setup Instructions

### Step 1: Create Twilio Account
1. Go to https://www.twilio.com/try-twilio
2. Sign up for a free trial account
3. Verify your email and phone number
4. You'll receive $15 in trial credit

### Step 2: Get WhatsApp Sandbox Access
1. Login to Twilio Console: https://console.twilio.com
2. Navigate to **Messaging** → **Try it out** → **Send a WhatsApp message**
3. Follow the instructions to:
   - Join the Twilio WhatsApp Sandbox
   - Send a WhatsApp message with the code shown (e.g., "join <your-code>") to the Twilio number
4. Your WhatsApp number is now connected to the sandbox

### Step 3: Get Your Twilio Credentials
1. From Twilio Console Dashboard, find:
   - **Account SID** (starts with AC...)
   - **Auth Token** (click to reveal)
   - **Twilio WhatsApp Number** (from WhatsApp Sandbox settings, format: +14155238886)

### Step 4: Configure Application
Edit `src/main/resources/application.properties`:

```properties
# Twilio WhatsApp Configuration
twilio.account.sid=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
twilio.auth.token=your_auth_token_here
twilio.phone.number=+14155238886
twilio.whatsapp.enabled=true
```

**Important:** Replace with your actual credentials!

### Step 5: Format Phone Numbers
Ensure customer phone numbers are in E.164 format:
- ✅ Correct: +919876543210
- ✅ Correct: +14155552671
- ❌ Wrong: 9876543210
- ❌ Wrong: (987) 654-3210

The system will auto-format Indian 10-digit numbers by adding +91.

### Step 6: Test WhatsApp Integration

#### Test 1: Send Invoice
1. Create a sale with a customer who has a valid phone number
2. Go to **Sales** → View Invoice
3. Click **"Send via WhatsApp"** button
4. Customer should receive formatted invoice on WhatsApp

#### Test 2: Expiry Alert (Optional - via code)
```java
@Autowired
private WhatsAppService whatsAppService;

// Send expiry alert
whatsAppService.sendExpiryAlert("Paracetamol 500mg", "31-12-2024", "+919876543210");
```

#### Test 3: Low Stock Alert (Optional - via code)
```java
whatsAppService.sendLowStockAlert("Crocin", 5, "+919876543210");
```

## Invoice Message Format
```
🏥 *MEDICAL STORE INVOICE*

📄 Invoice #: INV-000001
📅 Date: 17-11-2024 10:30

👤 Customer: John Doe

*ITEMS:*
━━━━━━━━━━━━━━━━━
Medicine: Paracetamol 500mg
Quantity: 10
Unit Price: ₹5.00
Subtotal: ₹50.00
Discount (10%): -₹5.00
GST (18%): +₹8.10
━━━━━━━━━━━━━━━━━
*TOTAL: ₹53.10*

💳 Payment: Cash

Thank you for your business!
Get well soon! 💊

_Medical Store System_
```

## Troubleshooting

### Issue: "WhatsApp is disabled" message
**Solution:** 
- Check if `twilio.whatsapp.enabled=true` in application.properties
- Verify Account SID is not "YOUR_ACCOUNT_SID" (default placeholder)
- Restart the application after configuration changes

### Issue: "Failed to send WhatsApp message"
**Solution:**
- Check Twilio Console for error logs: https://console.twilio.com/monitor/logs/errors
- Verify Auth Token and Account SID are correct
- Ensure customer phone number is in E.164 format
- Check if you have trial credit remaining

### Issue: Customer not receiving messages
**Solution:**
- In **Sandbox mode**, the customer must join the sandbox first (send "join <code>" to Twilio number)
- Check customer phone number format
- Verify customer's WhatsApp is active

### Issue: "Cannot send to unverified numbers"
**Solution:** 
- With Twilio trial account, you can only send to verified numbers
- Add customer numbers in Twilio Console: **Phone Numbers** → **Verified Caller IDs**
- Or upgrade to paid account for unrestricted sending

## Production Deployment

### For Production Use:
1. **Upgrade Twilio Account** - Convert from trial to paid account
2. **Get Approved WhatsApp Number** - Apply for official WhatsApp Business API access
   - Go to Twilio Console → Messaging → WhatsApp → Request Access
   - Fill business details and wait for approval (can take 1-2 weeks)
3. **Update Phone Number** - Replace sandbox number with your approved number
4. **Remove Trial Limitations** - No need to verify recipient numbers

### Security Best Practices:
1. **Never commit credentials** - Use environment variables:
   ```properties
   twilio.account.sid=${TWILIO_ACCOUNT_SID}
   twilio.auth.token=${TWILIO_AUTH_TOKEN}
   ```
2. **Use Spring Profiles** - Different configs for dev/prod
3. **Enable HTTPS** - Secure your webhook endpoints
4. **Rate Limiting** - Prevent API abuse

## API Usage Examples

### From Controller:
```java
@Autowired
private WhatsAppService whatsAppService;

@PostMapping("/sales/notify")
public String notifyCustomer(@RequestParam Long saleId) {
    Sale sale = saleService.getSaleById(saleId).orElseThrow();
    boolean sent = whatsAppService.sendInvoice(sale);
    return sent ? "Success" : "Failed";
}
```

### Check Configuration Status:
```java
if (whatsAppService.isConfigured()) {
    // WhatsApp is ready
} else {
    // Show setup instructions
}
```

## Cost Information
- **Trial Account**: $15 credit (free)
- **WhatsApp Messages**: ~$0.005 per message (Twilio pricing)
- **Production**: Upgrade plan starting at $20/month

## Support & Resources
- Twilio Documentation: https://www.twilio.com/docs/whatsapp
- WhatsApp Business API: https://www.twilio.com/whatsapp
- Twilio Support: https://support.twilio.com
- Application Logs: Check console for detailed error messages

## Features Included
✅ Auto-format phone numbers (Indian +91)
✅ Professional invoice formatting with emojis
✅ Print and WhatsApp buttons on invoice page
✅ Disabled button when WhatsApp not configured
✅ Loading spinner during message sending
✅ Error handling and user feedback
✅ Automatic retry logic (via Twilio SDK)
✅ Support for GST and discount display

## Next Steps
1. Configure Twilio credentials
2. Test with your own WhatsApp number
3. Add more customers with phone numbers
4. Create sales and send invoices
5. Monitor message delivery in Twilio Console

---

**Note:** This integration uses Twilio's WhatsApp Business API. Make sure to comply with WhatsApp's Business Policy and messaging guidelines.
