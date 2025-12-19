package ir.maktabsharif.onlineexam.service.impl;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.model.helper.UserUpdateChanges;
import ir.maktabsharif.onlineexam.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${server.port:8069}")
    private String serverPort;

    @Override
    public void sendUserApprovalEmail(User user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("baseUrl", "http://localhost:" + serverPort);
            context.setVariable("applicationTitle", "سیستم مدیریت آزمون آنلاین");

            String html = templateEngine.process("mail/user_approval", context);

            helper.setTo(user.getEmail());
            helper.setSubject("تایید حساب کاربری - سیستم مدیریت آزمون آنلاین");
            helper.setText(html, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("Approval email sent to user {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Error sending email to {}", user.getEmail(), e);
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }
    }

    @Override
    public void sendUserRejectionEmail(User user, String rejectionReason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("rejectionReason", rejectionReason);
            context.setVariable("baseUrl", "http://localhost:" + serverPort);
            context.setVariable("applicationTitle", "سیستم مدیریت آزمون آنلاین");

            String html = templateEngine.process("mail/user_rejection", context);

            helper.setTo(user.getEmail());
            helper.setSubject("رد ثبت نام - سیستم مدیریت آزمون آنلاین");
            helper.setText(html, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("Rejection email sent to user {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Error sending rejection email to {}", user.getEmail(), e);
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }
    }

    @Override
    public void sendPasswordResetCode(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context();
            context.setVariable("code", code);
            context.setVariable("baseUrl", "http://localhost:" + serverPort);
            context.setVariable("applicationTitle", "سیستم مدیریت آزمون آنلاین");
            context.setVariable("expirationMinutes", 5);

            String html = templateEngine.process("mail/password_reset_code", context);

            helper.setTo(email);
            helper.setSubject("کد فراموشی رمز عبور - سیستم مدیریت آزمون آنلاین");
            helper.setText(html, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("Password reset code sent to {}", email);
        } catch (MessagingException e) {
            log.error("Error sending password reset code email to {}", email, e);
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }
    }

    @Override
    public void sendUserUpdateEmail(User user, UserUpdateChanges changes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("changes", changes);
            context.setVariable("baseUrl", "http://localhost:" + serverPort);
            context.setVariable("applicationTitle", "سیستم مدیریت آزمون آنلاین");

            String html = templateEngine.process("mail/user_update", context);

            helper.setTo(user.getEmail());
            helper.setSubject("به‌روزرسانی اطلاعات حساب کاربری - سیستم مدیریت آزمون آنلاین");
            helper.setText(html, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("User update email sent to {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Error sending user update email to {}", user.getEmail(), e);
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }
    }
}

