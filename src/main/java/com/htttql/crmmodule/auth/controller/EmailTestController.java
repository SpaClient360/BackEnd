package com.htttql.crmmodule.auth.controller;

import com.htttql.crmmodule.auth.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller để test 2 email templates chính (chỉ dùng cho development)
 * - OTP Email với tone màu hồng
 * - Notification Email đẹp
 */
@RestController
@RequestMapping("/api/test/email")
@RequiredArgsConstructor
@Slf4j
public class EmailTestController {

    private final EmailService emailService;

    /**
     * Test gửi OTP email đẹp
     */
    @PostMapping("/otp")
    public ResponseEntity<Map<String, String>> testOTPEmail(@RequestParam String email) {
        try {
            // Gửi email OTP test với mã 123456
            emailService.sendOTPEmail(email, "123456");

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "🌸 Email OTP đẹp đã được gửi tới " + email,
                    "note", "Kiểm tra hộp thư của bạn để xem email template mới!"));

        } catch (Exception e) {
            log.error("Failed to send test OTP email: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Không thể gửi email: " + e.getMessage()));
        }
    }

    /**
     * Test gửi notification email đẹp
     */
    @PostMapping("/notification")
    public ResponseEntity<Map<String, String>> testNotificationEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "Chào mừng bạn!") String title,
            @RequestParam(defaultValue = "Cảm ơn bạn đã sử dụng dịch vụ CRM của chúng tôi. Chúng tôi rất vui khi được phục vụ bạn!") String message,
            @RequestParam(required = false) String actionText,
            @RequestParam(required = false) String actionUrl) {
        try {
            // Gửi notification email với template đẹp
            emailService.sendNotificationEmail(email, title, message, actionText, actionUrl);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "🌸 Email thông báo đẹp đã được gửi tới " + email,
                    "note", "Kiểm tra hộp thư để xem email template mới với tone màu hồng!"));

        } catch (Exception e) {
            log.error("Failed to send test notification email: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Không thể gửi email: " + e.getMessage()));
        }
    }

}
