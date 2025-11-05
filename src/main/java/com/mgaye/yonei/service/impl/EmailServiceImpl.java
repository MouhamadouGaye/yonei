package com.mgaye.yonei.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.mgaye.yonei.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.support-email}")
    private String supportEmail;

    @Value("${app.company-name}")
    private String companyName;

    @Value("${app.email.from:mgayeeeeee@gmail.com}")
    private String fromEmail;

    @Value("${app.admin-email}")
    private String adminEmail;

    @Value("${app.admin-email}")
    private String noreplyEmail;

    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    // === ACCOUNT MANAGEMENT ===

    @Async
    @Override
    public void sendWelcomeEmail(String toEmail, String username) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("companyName", companyName);
        context.setVariable("supportEmail", supportEmail);

        String subject = "Welcome to " + companyName + "!";
        String htmlContent = templateEngine.process("emails/welcome", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    // public void sendWelcomeEmail(String toEmail, String username) {
    // try {
    // logger.info("Sending welcome email to: {}", toEmail);

    // Context context = new Context();
    // context.setVariable("username", username);
    // context.setVariable("companyName", companyName);
    // context.setVariable("supportEmail", "support@moneytransfer.com");
    // context.setVariable("subject", "Welcome to " + companyName);

    // String htmlContent = templateEngine.process("emails/welcome", context);

    // MimeMessage message = mailSender.createMimeMessage();
    // MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    // helper.setFrom(fromEmail, companyName);
    // helper.setTo(toEmail);
    // helper.setSubject("Welcome to " + companyName);
    // helper.setText(htmlContent, true);

    // mailSender.send(message);
    // logger.info("Welcome email sent successfully to: {}", toEmail);

    // } catch (Exception e) {
    // logger.error("Failed to send welcome email to {}: {}", toEmail,
    // e.getMessage());
    // // Don't throw - welcome email failure shouldn't block registration
    // }
    // }

    @Async
    @Override
    public void sendAccountVerificationEmail(
            String toEmail, String verificationToken, String name) { // the username - name

        String verificationUrl = baseUrl + "/api/users/verify-email-page?token=" +
                verificationToken;

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("verificationUrl", verificationUrl);
        context.setVariable("companyName", companyName);

        String subject = "Verify Your Email - " + companyName;
        String htmlContent = templateEngine.process("emails/account-verification-standalone",
                context);

        sendEmail(toEmail, subject, htmlContent);
    }

    // public void sendAccountVerificationEmail(String toEmail, String
    // verificationToken, String username) {
    // try {
    // logger.info("Sending verification email to: {}", toEmail);

    // Context context = new Context();
    // context.setVariable("username", username);
    // context.setVariable("verificationUrl", baseUrl + "/verify-email?token=" +
    // verificationToken);
    // context.setVariable("companyName", companyName);
    // context.setVariable("supportEmail", "support@moneytransfer.com");
    // context.setVariable("subject", "Verify Your Email - " + companyName);

    // String htmlContent = templateEngine.process("emails/account-verification",
    // context);

    // MimeMessage message = mailSender.createMimeMessage();
    // MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    // helper.setFrom(fromEmail, companyName);
    // helper.setTo(toEmail);
    // helper.setSubject("Verify Your Email - " + companyName);
    // helper.setText(htmlContent, true);

    // mailSender.send(message);
    // logger.info("Verification email sent successfully to: {}", toEmail);

    // } catch (Exception e) {
    // logger.error("Failed to send verification email to {}: {}", toEmail,
    // e.getMessage());
    // // Don't throw - just log the error
    // }
    // }

    @Async
    @Override
    public void sendAccountVerifiedEmail(String toEmail, String username) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("companyName", companyName);

        String subject = "Email Verified Successfully - " + companyName;
        String htmlContent = templateEngine.process("emails/account-verified-standalone", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    // === SECURITY & AUTHENTICATION ===

    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken, String username) {
        String resetUrl = baseUrl + "/reset-password?token=" + resetToken;

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("resetUrl", resetUrl);
        context.setVariable("companyName", companyName);

        String subject = "Password Reset Request - " + companyName;
        String htmlContent = templateEngine.process("emails/password-reset", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendPasswordChangedEmail(String toEmail, String username) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("companyName", companyName);
        context.setVariable("supportEmail", supportEmail);

        String subject = "Password Changed - " + companyName;
        String htmlContent = templateEngine.process("emails/password-changed", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendEmailChangeVerification(String toEmail, String verificationToken, String username) {
        String verificationUrl = baseUrl + "/verify-email-change?token=" + verificationToken;

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("verificationUrl", verificationUrl);
        context.setVariable("companyName", companyName);

        String subject = "Confirm Your Email Change - " + companyName;
        String htmlContent = templateEngine.process("emails/email-change-verification", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendEmailChangeConfirmation(String toEmail, String oldEmail, String username) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("oldEmail", oldEmail);
        context.setVariable("newEmail", toEmail);
        context.setVariable("companyName", companyName);
        context.setVariable("supportEmail", supportEmail);

        String subject = "Email Address Updated - " + companyName;
        String htmlContent = templateEngine.process("emails/email-change-confirmation", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendTwoFactorCodeEmail(String toEmail, String code, String username) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("code", code);
        context.setVariable("companyName", companyName);

        String subject = "Your Verification Code - " + companyName;
        String htmlContent = templateEngine.process("emails/two-factor-code", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendSuspiciousActivityAlert(String toEmail, String activityDescription, String username) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("activityDescription", activityDescription);
        context.setVariable("companyName", companyName);
        context.setVariable("supportEmail", supportEmail);

        String subject = "Suspicious Activity Detected - " + companyName;
        String htmlContent = templateEngine.process("emails/suspicious-activity", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendPasswordChangeNotification(String toEmail, String username) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("companyName", companyName);
        context.setVariable("supportEmail", supportEmail);
        context.setVariable("timestamp", java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")));

        String subject = "Password Changed - Security Notification";
        String htmlContent = templateEngine.process("emails/password-change-notification", context);

        sendEmail(toEmail, subject, htmlContent);
    }
    // === TRANSACTION EMAILS ===

    @Async
    @Override
    public void sendTransferSentConfirmation(String toEmail, String username, String recipientName,
            String amount, String currency, String transactionId) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("recipientName", recipientName);
        context.setVariable("amount", amount);
        context.setVariable("currency", currency);
        context.setVariable("transactionId", transactionId);
        context.setVariable("companyName", companyName);

        String subject = "Transfer Sent - " + amount + " " + currency;
        String htmlContent = templateEngine.process("emails/transfer-sent", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendTransferReceivedNotification(String toEmail, String username, String senderName,
            String amount, String currency, String transactionId) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("senderName", senderName);
        context.setVariable("amount", amount);
        context.setVariable("currency", currency);
        context.setVariable("transactionId", transactionId);
        context.setVariable("companyName", companyName);

        String subject = "You Received " + amount + " " + currency;
        String htmlContent = templateEngine.process("emails/transfer-received", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendTransferFailedNotification(String toEmail, String username, String reason,
            String amount, String currency) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("reason", reason);
        context.setVariable("amount", amount);
        context.setVariable("currency", currency);
        context.setVariable("companyName", companyName);
        context.setVariable("supportEmail", supportEmail);

        String subject = "Transfer Failed - " + amount + " " + currency;
        String htmlContent = templateEngine.process("emails/transfer-failed", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendTransferPendingReview(String toEmail, String username, String amount,
            String currency, String transactionId) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("amount", amount);
        context.setVariable("currency", currency);
        context.setVariable("transactionId", transactionId);
        context.setVariable("companyName", companyName);
        context.setVariable("supportEmail", supportEmail);

        String subject = "Transfer Under Review - " + amount + " " + currency;
        String htmlContent = templateEngine.process("emails/transfer-pending", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    // === PAYMENT METHOD EMAILS ===

    @Async
    @Override
    public void sendPaymentMethodAdded(String toEmail, String username, String cardLast4, String cardBrand) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("cardLast4", cardLast4);
        context.setVariable("cardBrand", cardBrand);
        context.setVariable("companyName", companyName);
        context.setVariable("supportEmail", supportEmail);

        String subject = "New Payment Method Added - " + companyName;
        String htmlContent = templateEngine.process("emails/payment-method-added", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendPaymentMethodRemoved(String toEmail, String username, String cardLast4, String cardBrand) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("cardLast4", cardLast4);
        context.setVariable("cardBrand", cardBrand);
        context.setVariable("companyName", companyName);
        context.setVariable("supportEmail", supportEmail);

        String subject = "Payment Method Removed - " + companyName;
        String htmlContent = templateEngine.process("emails/payment-method-removed", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    // === SECURITY ALERTS ===

    @Async
    @Override
    public void sendNewDeviceLoginAlert(String toEmail, String username, String deviceInfo, String location) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("deviceInfo", deviceInfo);
        context.setVariable("location", location);
        context.setVariable("companyName", companyName);
        context.setVariable("supportEmail", supportEmail);

        String subject = "New Login Detected - " + companyName;
        String htmlContent = templateEngine.process("emails/new-device-login", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendLargeTransferAlert(String toEmail, String username, String amount, String currency) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("amount", amount);
        context.setVariable("currency", currency);
        context.setVariable("companyName", companyName);
        context.setVariable("supportEmail", supportEmail);

        String subject = "Large Transfer Alert - " + amount + " " + currency;
        String htmlContent = templateEngine.process("emails/large-transfer-alert", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendBalanceThresholdAlert(String toEmail, String username, String balance, String threshold) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("balance", balance);
        context.setVariable("threshold", threshold);
        context.setVariable("companyName", companyName);

        String subject = "Balance Alert - " + companyName;
        String htmlContent = templateEngine.process("emails/balance-threshold", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    // === COMPLIANCE & AUDIT EMAILS ===

    @Async
    @Override
    public void sendMonthlyStatement(String toEmail, String username, String monthYear, String statementUrl) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("monthYear", monthYear);
        context.setVariable("statementUrl", statementUrl);
        context.setVariable("companyName", companyName);

        String subject = "Your Monthly Statement - " + monthYear;
        String htmlContent = templateEngine.process("emails/monthly-statement", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendTaxDocumentAvailable(String toEmail, String username, String documentType, String year) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("documentType", documentType);
        context.setVariable("year", year);
        context.setVariable("companyName", companyName);

        String subject = documentType + " Available - Tax Year " + year;
        String htmlContent = templateEngine.process("emails/tax-document", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendComplianceUpdate(String toEmail, String username, String updateDetails) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("updateDetails", updateDetails);
        context.setVariable("companyName", companyName);

        String subject = "Important Compliance Update - " + companyName;
        String htmlContent = templateEngine.process("emails/compliance-update", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    // === ADMIN & SYSTEM EMAILS ===

    @Async
    @Override
    public void sendAdminSecurityAlert(String alertType, String description, String severity) {
        Context context = new Context();
        context.setVariable("alertType", alertType);
        context.setVariable("description", description);
        context.setVariable("severity", severity);
        context.setVariable("timestamp", java.time.LocalDateTime.now());

        String subject = "SECURITY ALERT [" + severity + "]: " + alertType;
        String htmlContent = templateEngine.process("emails/admin-security-alert", context);

        sendEmail(adminEmail, subject, htmlContent);
    }

    @Async
    @Override
    public void sendSystemMaintenanceNotice(String toEmail, String username, String maintenanceSchedule) {
        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("maintenanceSchedule", maintenanceSchedule);
        context.setVariable("companyName", companyName);
        context.setVariable("supportEmail", supportEmail);

        String subject = "Scheduled Maintenance Notice - " + companyName;
        String htmlContent = templateEngine.process("emails/system-maintenance", context);

        sendEmail(toEmail, subject, htmlContent);
    }

    // === PRIVATE HELPER METHOD ===

    private void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // FIX: Use the noreplyEmail directly or extract domain properly
            String fromEmail = getFromEmail();
            try {
                helper.setFrom(fromEmail, companyName);
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(fromEmail);
            }
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email to " + toEmail, e);
        }
    }

    /**
     * Get the from email address
     * If noreplyEmail is configured, use it directly
     * Otherwise, construct it from supportEmail domain
     */
    private String getFromEmail() {
        // if (noreplyEmail != null && !noreplyEmail.trim().isEmpty()) {
        // return noreplyEmail.trim();
        // }

        // // Fallback: construct noreply email from support email domain
        // String domain = getDomainFromEmail(supportEmail);
        // return "noreply@" + domain;
        return fromEmail;
    }

    /**
     * Extract domain from email address
     * Example: "support@moneytransfer.com" -> "moneytransfer.com"
     */
    private String getDomainFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "moneytransfer.com"; // default domain
        }
        return email.substring(email.indexOf('@') + 1);
    }
}