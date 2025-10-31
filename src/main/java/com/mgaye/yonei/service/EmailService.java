package com.mgaye.yonei.service;

public interface EmailService {

        // Account Management Emails
        void sendWelcomeEmail(String toEmail, String username);

        void sendAccountVerificationEmail(String toEmail, String verificationToken, String username);

        void sendAccountVerifiedEmail(String toEmail, String username);

        // Security & Authentication Emails
        void sendPasswordResetEmail(String toEmail, String resetToken, String username);

        void sendPasswordChangedEmail(String toEmail, String username);

        void sendEmailChangeVerification(String toEmail, String verificationToken, String username);

        void sendEmailChangeConfirmation(String toEmail, String oldEmail, String username);

        void sendTwoFactorCodeEmail(String toEmail, String code, String username);

        void sendSuspiciousActivityAlert(String toEmail, String activityDescription, String username);

        // ✅ ADD THIS MISSING METHOD
        void sendPasswordChangeNotification(String toEmail, String username);

        // Transaction Emails
        void sendTransferSentConfirmation(String toEmail, String username, String recipientName,
                        String amount, String currency, String transactionId);

        void sendTransferReceivedNotification(String toEmail, String username, String senderName,
                        String amount, String currency, String transactionId);

        void sendTransferFailedNotification(String toEmail, String username, String reason,
                        String amount, String currency);

        void sendTransferPendingReview(String toEmail, String username, String amount,
                        String currency, String transactionId);

        // Payment Method Emails
        void sendPaymentMethodAdded(String toEmail, String username, String cardLast4, String cardBrand);

        void sendPaymentMethodRemoved(String toEmail, String username, String cardLast4, String cardBrand);

        // Security Alerts
        void sendNewDeviceLoginAlert(String toEmail, String username, String deviceInfo, String location);

        void sendLargeTransferAlert(String toEmail, String username, String amount, String currency);

        void sendBalanceThresholdAlert(String toEmail, String username, String balance, String threshold);

        // Compliance & Audit Emails
        void sendMonthlyStatement(String toEmail, String username, String monthYear, String statementUrl);

        void sendTaxDocumentAvailable(String toEmail, String username, String documentType, String year);

        void sendComplianceUpdate(String toEmail, String username, String updateDetails);

        // Admin & System Emails
        void sendAdminSecurityAlert(String alertType, String description, String severity);

        void sendSystemMaintenanceNotice(String toEmail, String username, String maintenanceSchedule);
}