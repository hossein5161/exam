package ir.maktabsharif.onlineexam.service.impl;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.model.helper.UserUpdateChanges;
import ir.maktabsharif.onlineexam.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${server.port:8069}")
    private String serverPort;

    @Override
    public void sendUserApprovalEmail(User user, Locale locale) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context(locale);
            context.setVariable("user", user);
            context.setVariable("baseUrl", "http://localhost:" + serverPort);
            String applicationTitle = messageSource.getMessage("application.title", null, locale);
            context.setVariable("applicationTitle", applicationTitle);

            String html = templateEngine.process("mail/user_approval", context);

            helper.setTo(user.getEmail());
            String subject = messageSource.getMessage("mail.user.approval.subject", null, locale);
            helper.setSubject(subject);
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
    public void sendUserRejectionEmail(User user, String rejectionReason, Locale locale) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context(locale);
            context.setVariable("user", user);
            context.setVariable("rejectionReason", rejectionReason);
            context.setVariable("baseUrl", "http://localhost:" + serverPort);
            String applicationTitle = messageSource.getMessage("application.title", null, locale);
            context.setVariable("applicationTitle", applicationTitle);

            String html = templateEngine.process("mail/user_rejection", context);

            helper.setTo(user.getEmail());
            String subject = messageSource.getMessage("mail.user.rejection.subject", null, locale);
            helper.setSubject(subject);
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
    public void sendPasswordResetCode(String email, String code, Locale locale) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context(locale);
            context.setVariable("code", code);
            context.setVariable("baseUrl", "http://localhost:" + serverPort);
            String applicationTitle = messageSource.getMessage("application.title", null, locale);
            context.setVariable("applicationTitle", applicationTitle);
            context.setVariable("expirationMinutes", 5);

            String html = templateEngine.process("mail/password_reset_code", context);

            helper.setTo(email);
            String subject = messageSource.getMessage("mail.password.reset.subject", null, locale);
            helper.setSubject(subject);
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
    public void sendUserUpdateEmail(User user, UserUpdateChanges changes, Locale locale) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context(locale);
            context.setVariable("user", user);
            context.setVariable("changes", changes);
            context.setVariable("baseUrl", "http://localhost:" + serverPort);
            String applicationTitle = messageSource.getMessage("application.title", null, locale);
            context.setVariable("applicationTitle", applicationTitle);

            String html = templateEngine.process("mail/user_update", context);

            helper.setTo(user.getEmail());
            String subject = messageSource.getMessage("mail.user.update.subject", null, locale);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("User update email sent to {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Error sending user update email to {}", user.getEmail(), e);
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }
    }

    @Override
    public void sendUserDeletionEmail(User user, String deletionReason, Locale locale) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context context = new Context(locale);
            context.setVariable("user", user);
            context.setVariable("deletionReason", deletionReason);
            context.setVariable("baseUrl", "http://localhost:" + serverPort);
            String applicationTitle = messageSource.getMessage("application.title", null, locale);
            context.setVariable("applicationTitle", applicationTitle);

            String html = templateEngine.process("mail/user_deletion", context);

            helper.setTo(user.getEmail());
            String subject = messageSource.getMessage("mail.user.deletion.subject", null, locale);
            helper.setSubject(subject);
            helper.setText(html, true);
            helper.setFrom(fromEmail);

            mailSender.send(message);
            log.info("Deletion email sent to user {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Error sending deletion email to {}", user.getEmail(), e);
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }
    }
}

