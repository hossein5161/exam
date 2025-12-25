package ir.maktabsharif.onlineexam.controller;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.repository.UserRepository;
import ir.maktabsharif.onlineexam.service.EmailService;
import ir.maktabsharif.onlineexam.service.PasswordResetService;
import ir.maktabsharif.onlineexam.service.UserService;
import ir.maktabsharif.onlineexam.util.PasswordValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/password-reset")
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordResetService passwordResetService;
    private final EmailService emailService;
    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping("/forgot")
    public String forgotPasswordPage() {
        return "password_reset/forgot_password";
    }

    @PostMapping("/forgot")
    public String sendResetCode(@RequestParam String email, RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            if (userRepository.findByEmail(email).isEmpty()) {
                String errorMessage = messageSource.getMessage("password.forgot.email.not.found", null, locale);
                redirectAttributes.addFlashAttribute("error", errorMessage);
                return "redirect:/password-reset/forgot";
            }

            String code = passwordResetService.generateAndStoreResetCode(email);

            emailService.sendPasswordResetCode(email, code, locale);

            String successMessage = messageSource.getMessage("password.forgot.code.sent", null, locale);
            redirectAttributes.addFlashAttribute("success", successMessage);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/password-reset/verify-code";
        } catch (Exception e) {
            log.error("Error sending password reset code", e);
            String errorMessage = messageSource.getMessage("password.forgot.send.error", 
                new Object[]{e.getMessage()}, locale);
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/password-reset/forgot";
        }
    }

    @GetMapping("/verify-code")
    public String verifyCodePage(@ModelAttribute("email") String email, Model model) {
        if (email == null || email.isEmpty()) {
            return "redirect:/password-reset/forgot";
        }
        model.addAttribute("email", email);
        return "password_reset/verify_code";
    }

    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam String email,
                            @RequestParam String code,
                            RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            if (!passwordResetService.validateResetCode(email, code)) {
                String errorMessage = messageSource.getMessage("password.verify.code.invalid", null, locale);
                redirectAttributes.addFlashAttribute("error", errorMessage);
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/password-reset/verify-code";
            }

            passwordResetService.deleteResetCode(email);

            redirectAttributes.addFlashAttribute("email", email);
            String successMessage = messageSource.getMessage("password.verify.code.success", null, locale);
            redirectAttributes.addFlashAttribute("success", successMessage);
            return "redirect:/password-reset/reset";
        } catch (Exception e) {
            log.error("Error verifying code", e);
            String errorMessage = messageSource.getMessage("password.verify.code.error", 
                new Object[]{e.getMessage()}, locale);
            redirectAttributes.addFlashAttribute("error", errorMessage);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/password-reset/verify-code";
        }
    }

    @GetMapping("/reset")
    public String resetPasswordPage(@ModelAttribute("email") String email, Model model) {
        if (email == null || email.isEmpty()) {
            return "redirect:/password-reset/forgot";
        }
        model.addAttribute("email", email);
        return "password_reset/reset_password";
    }

    @PostMapping("/reset")
    public String resetPassword(@RequestParam String email,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                String mismatchMessage = messageSource.getMessage(
                    "validation.password.mismatch", 
                    null, 
                    LocaleContextHolder.getLocale()
                );
                redirectAttributes.addFlashAttribute("error", mismatchMessage);
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/password-reset/reset";
            }

            String passwordError = PasswordValidationUtil.validate(
                newPassword, 
                messageSource, 
                LocaleContextHolder.getLocale()
            );
            if (passwordError != null) {
                redirectAttributes.addFlashAttribute("error", passwordError);
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/password-reset/reset";
            }

            var userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                Locale locale = LocaleContextHolder.getLocale();
                String errorMessage = messageSource.getMessage("password.reset.user.not.found", null, locale);
                redirectAttributes.addFlashAttribute("error", errorMessage);
                return "redirect:/password-reset/forgot";
            }

            var user = userOptional.get();

            User tempUser = new User();
            tempUser.setPassword(newPassword);
            userService.updateUser(user.getId(), tempUser);

            Locale locale = LocaleContextHolder.getLocale();
            String successMessage = messageSource.getMessage("password.reset.success", null, locale);
            redirectAttributes.addFlashAttribute("success", successMessage);
            return "redirect:/login";
        } catch (Exception e) {
            log.error("Error occurred while changing the password", e);
            Locale locale = LocaleContextHolder.getLocale();
            String errorMessage = messageSource.getMessage("password.reset.error", 
                new Object[]{e.getMessage()}, locale);
            redirectAttributes.addFlashAttribute("error", errorMessage);
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/password-reset/reset";
        }
    }
}

