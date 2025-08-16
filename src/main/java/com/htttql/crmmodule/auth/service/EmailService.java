package com.htttql.crmmodule.auth.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Service for sending emails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Send OTP email to user with beautiful HTML template
     * 
     * @param toEmail recipient email address
     * @param otpCode the OTP code to send
     */
    public void sendOTPEmail(String toEmail, String otpCode) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("tdthanh.dev2025@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("🌸 Mã OTP đăng nhập - CRM System");

            // Create beautiful HTML email content with pink theme
            String htmlContent = buildBeautifulOTPEmailContent(otpCode);
            helper.setText(htmlContent, true); // true = HTML content

            // Send email
            mailSender.send(mimeMessage);

            // Log success (without sensitive information)
            log.info("Beautiful OTP email sent successfully to: {}", maskEmail(toEmail));

        } catch (Exception e) {
            // Log error (without sensitive information)
            log.error("Failed to send OTP email to: {}. Error: {}",
                    maskEmail(toEmail), e.getMessage());
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        }
    }

    /**
     * Build beautiful HTML OTP email content with pink theme
     * 
     * @param otpCode the OTP code
     * @return formatted HTML email content
     */
    private String buildBeautifulOTPEmailContent(String otpCode) {
        return String.format(
                """
                        <!DOCTYPE html>
                        <html lang="vi">
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <title>Mã OTP - CRM System</title>
                            <style>
                                * {
                                    margin: 0;
                                    padding: 0;
                                    box-sizing: border-box;
                                }
                                body {
                                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                                    background: linear-gradient(135deg, #ffeef7 0%%, #fff0f8 100%%);
                                    padding: 20px;
                                    line-height: 1.6;
                                }
                                .container {
                                    max-width: 600px;
                                    margin: 0 auto;
                                    background: white;
                                    border-radius: 20px;
                                    overflow: hidden;
                                    box-shadow: 0 20px 40px rgba(226, 70, 135, 0.1);
                                    border: 2px solid #ffc0cb;
                                }
                                .header {
                                    background: linear-gradient(135deg, #ff69b4, #ff1493);
                                    color: white;
                                    padding: 30px;
                                    text-align: center;
                                    position: relative;
                                }
                                .header::before {
                                    content: '';
                                    position: absolute;
                                    top: -50px;
                                    left: -50px;
                                    width: 100px;
                                    height: 100px;
                                    background: rgba(255, 255, 255, 0.1);
                                    border-radius: 50%%;
                                }
                                .header::after {
                                    content: '';
                                    position: absolute;
                                    bottom: -30px;
                                    right: -30px;
                                    width: 60px;
                                    height: 60px;
                                    background: rgba(255, 255, 255, 0.1);
                                    border-radius: 50%%;
                                }
                                .header h1 {
                                    font-size: 28px;
                                    margin-bottom: 10px;
                                    font-weight: 600;
                                }
                                .emoji {
                                    font-size: 40px;
                                    margin-bottom: 15px;
                                    display: block;
                                }
                                .content {
                                    padding: 40px 30px;
                                    text-align: center;
                                }
                                .greeting {
                                    font-size: 20px;
                                    color: #d63384;
                                    margin-bottom: 25px;
                                    font-weight: 500;
                                }
                                .otp-container {
                                    background: linear-gradient(135deg, #ffeef7, #fff0f8);
                                    border: 3px dashed #ff69b4;
                                    border-radius: 15px;
                                    padding: 30px;
                                    margin: 30px 0;
                                    position: relative;
                                }
                                .otp-label {
                                    color: #d63384;
                                    font-size: 16px;
                                    font-weight: 600;
                                    margin-bottom: 15px;
                                    text-transform: uppercase;
                                    letter-spacing: 1px;
                                }
                                .otp-code {
                                    font-size: 36px;
                                    font-weight: 700;
                                    color: #e91e63;
                                    letter-spacing: 8px;
                                    font-family: 'Courier New', monospace;
                                    text-shadow: 2px 2px 4px rgba(233, 30, 99, 0.2);
                                    margin: 15px 0;
                                }
                                .timer {
                                    background: #ff1493;
                                    color: white;
                                    padding: 8px 20px;
                                    border-radius: 25px;
                                    font-size: 14px;
                                    font-weight: 600;
                                    display: inline-block;
                                    margin-top: 15px;
                                }
                                .instructions {
                                    color: #666;
                                    font-size: 16px;
                                    margin: 25px 0;
                                    line-height: 1.8;
                                }
                                .warning {
                                    background: #fff3cd;
                                    border: 1px solid #ffeaa7;
                                    border-radius: 10px;
                                    padding: 20px;
                                    margin: 25px 0;
                                    color: #856404;
                                    font-size: 14px;
                                }
                                .footer {
                                    background: #f8f9fa;
                                    padding: 25px;
                                    text-align: center;
                                    border-top: 1px solid #ffc0cb;
                                }
                                .footer-text {
                                    color: #6c757d;
                                    font-size: 14px;
                                    margin-bottom: 10px;
                                }
                                .signature {
                                    color: #d63384;
                                    font-weight: 600;
                                    font-size: 16px;
                                }
                                .hearts {
                                    color: #ff69b4;
                                    margin: 0 5px;
                                }
                                @media (max-width: 600px) {
                                    .container {
                                        margin: 10px;
                                        border-radius: 15px;
                                    }
                                    .content {
                                        padding: 25px 20px;
                                    }
                                    .otp-code {
                                        font-size: 28px;
                                        letter-spacing: 4px;
                                    }
                                }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <span class="emoji">🌸</span>
                                    <h1>Xác Thực OTP</h1>
                                    <p>CRM System - Bảo mật cao cấp</p>
                                </div>

                                <div class="content">
                                    <div class="greeting">
                                        Xin chào! 👋
                                    </div>

                                    <p class="instructions">
                                        Chúng tôi đã nhận được yêu cầu đăng nhập vào tài khoản của bạn.<br>
                                        Vui lòng sử dụng mã OTP bên dưới để hoàn tất quá trình xác thực:
                                    </p>

                                    <div class="otp-container">
                                        <div class="otp-label">🔐 Mã OTP của bạn</div>
                                        <div class="otp-code">%s</div>
                                        <div class="timer">⏱️ Có hiệu lực trong 5 phút</div>
                                    </div>

                                    <div class="warning">
                                        <strong>⚠️ Lưu ý quan trọng:</strong><br>
                                        • Mã OTP này chỉ sử dụng được 1 lần<br>
                                        • Không chia sẻ mã này với bất kỳ ai<br>
                                        • Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email
                                    </div>

                                    <p class="instructions">
                                        Nếu bạn gặp khó khăn hoặc cần hỗ trợ, vui lòng liên hệ với đội ngũ hỗ trợ khách hàng của chúng tôi.
                                    </p>
                                </div>

                                <div class="footer">
                                    <p class="footer-text">
                                        Email này được gửi tự động từ hệ thống CRM
                                    </p>
                                    <p class="signature">
                                        <span class="hearts">💖</span>
                                        Trân trọng, CRM System Team
                                        <span class="hearts">💖</span>
                                    </p>
                                </div>
                            </div>
                        </body>
                        </html>
                        """,
                otpCode);
    }

    /**
     * Build OTP email content (fallback plain text)
     * 
     * @param otpCode the OTP code
     * @return formatted email content
     */
    private String buildOTPEmailContent(String otpCode) {
        return String.format(
                "Xin chào,\n\n" +
                        "Mã OTP của bạn là: %s\n\n" +
                        "Mã này sẽ hết hạn sau 5 phút.\n" +
                        "Vui lòng không chia sẻ mã này với ai khác.\n\n" +
                        "Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.\n\n" +
                        "Trân trọng,\n" +
                        "CRM System Team",
                otpCode);
    }

    /**
     * Mask email for logging (security purpose)
     * Example: test@gmail.com -> t***@gmail.com
     * 
     * @param email the email to mask
     * @return masked email
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 3) {
            return "***";
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "***";
        }

        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***" + domain;
        }

        return localPart.charAt(0) + "***" + domain;
    }

    /**
     * Send general email (plain text)
     * 
     * @param toEmail recipient email
     * @param subject email subject
     * @param content email content
     */
    public void sendEmail(String toEmail, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("tdthanh.dev2025@gmail.com");
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);

            log.info("Plain text email sent successfully to: {} with subject: {}",
                    maskEmail(toEmail), subject);

        } catch (Exception e) {
            log.error("Failed to send email to: {} with subject: {}. Error: {}",
                    maskEmail(toEmail), subject, e.getMessage());
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        }
    }

    /**
     * Send beautiful HTML email with pink theme
     * 
     * @param toEmail     recipient email
     * @param subject     email subject
     * @param htmlContent HTML email content
     */
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("tdthanh.dev2025@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML content

            mailSender.send(mimeMessage);

            log.info("Beautiful HTML email sent successfully to: {} with subject: {}",
                    maskEmail(toEmail), subject);

        } catch (Exception e) {
            log.error("Failed to send HTML email to: {} with subject: {}. Error: {}",
                    maskEmail(toEmail), subject, e.getMessage());
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        }
    }

    /**
     * Send notification email with beautiful template
     * 
     * @param toEmail    recipient email
     * @param title      notification title
     * @param message    notification message
     * @param actionText optional action button text
     * @param actionUrl  optional action button URL
     */
    public void sendNotificationEmail(String toEmail, String title, String message,
            String actionText, String actionUrl) {
        try {
            String subject = "🌸 " + title + " - CRM System";
            String htmlContent = buildNotificationEmailContent(title, message, actionText, actionUrl);
            sendHtmlEmail(toEmail, subject, htmlContent);

        } catch (Exception e) {
            log.error("Failed to send notification email to: {}. Error: {}",
                    maskEmail(toEmail), e.getMessage());
            throw new RuntimeException("Không thể gửi email thông báo. Vui lòng thử lại sau.");
        }
    }

    /**
     * Build beautiful notification email template
     */
    private String buildNotificationEmailContent(String title, String message,
            String actionText, String actionUrl) {
        String actionButton = "";
        if (actionText != null && actionUrl != null) {
            actionButton = String.format("""
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="
                            background: linear-gradient(135deg, #ff69b4, #ff1493);
                            color: white;
                            padding: 15px 30px;
                            text-decoration: none;
                            border-radius: 25px;
                            font-weight: 600;
                            display: inline-block;
                            box-shadow: 0 5px 15px rgba(255, 105, 180, 0.3);
                            transition: transform 0.2s;
                        ">%s</a>
                    </div>
                    """, actionUrl, actionText);
        }

        return String.format("""
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background: linear-gradient(135deg, #ffeef7 0%%, #fff0f8 100%%);
                            margin: 0;
                            padding: 20px;
                            line-height: 1.6;
                        }
                        .container {
                            max-width: 600px;
                            margin: 0 auto;
                            background: white;
                            border-radius: 20px;
                            overflow: hidden;
                            box-shadow: 0 20px 40px rgba(226, 70, 135, 0.1);
                            border: 2px solid #ffc0cb;
                        }
                        .header {
                            background: linear-gradient(135deg, #ff69b4, #ff1493);
                            color: white;
                            padding: 30px;
                            text-align: center;
                        }
                        .content {
                            padding: 40px 30px;
                            color: #333;
                        }
                        .message {
                            font-size: 16px;
                            line-height: 1.8;
                            margin: 20px 0;
                            color: #555;
                        }
                        .footer {
                            background: #f8f9fa;
                            padding: 25px;
                            text-align: center;
                            border-top: 1px solid #ffc0cb;
                            color: #6c757d;
                            font-size: 14px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🌸 %s</h1>
                            <p>CRM System</p>
                        </div>
                        <div class="content">
                            <div class="message">%s</div>
                            %s
                        </div>
                        <div class="footer">
                            <p>💖 Trân trọng, CRM System Team 💖</p>
                        </div>
                    </div>
                </body>
                </html>
                """, title, title, message, actionButton);
    }
}