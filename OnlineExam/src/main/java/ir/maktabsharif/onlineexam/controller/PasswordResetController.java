package ir.maktabsharif.onlineexam.controller;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.repository.UserRepository;
import ir.maktabsharif.onlineexam.service.EmailService;
import ir.maktabsharif.onlineexam.service.PasswordResetService;
import ir.maktabsharif.onlineexam.service.UserService;
import ir.maktabsharif.onlineexam.util.PasswordValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/password-reset")
public class PasswordResetController {

    private final UserRepository userRepository;
    private final PasswordResetService passwordResetService;
    private final EmailService emailService;
    private final UserService userService;

    @GetMapping("/forgot")
    public String forgotPasswordPage() {
        return "password_reset/forgot_password";
    }

    @PostMapping("/forgot")
    public String sendResetCode(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            if (userRepository.findByEmail(email).isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "ایمیل وارد شده در سیستم یافت نشد");
                return "redirect:/password-reset/forgot";
            }

            String code = passwordResetService.generateAndStoreResetCode(email);

            emailService.sendPasswordResetCode(email, code);

            redirectAttributes.addFlashAttribute("success", 
                "کد فراموشی رمز عبور به ایمیل شما ارسال شد. لطفاً ایمیل خود را بررسی کنید.");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/password-reset/verify-code";
        } catch (Exception e) {
            log.error("Error sending password reset code", e);
            redirectAttributes.addFlashAttribute("error", 
                "خطا در ارسال کد: " + e.getMessage());
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
        try {
            if (!passwordResetService.validateResetCode(email, code)) {
                redirectAttributes.addFlashAttribute("error", 
                    "کد وارد شده نامعتبر است یا منقضی شده است. لطفاً دوباره تلاش کنید.");
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/password-reset/verify-code";
            }

            passwordResetService.deleteResetCode(email);

            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("success", "کد با موفقیت تایید شد");
            return "redirect:/password-reset/reset";
        } catch (Exception e) {
            log.error("خطا در تایید کد", e);
            redirectAttributes.addFlashAttribute("error", "خطا در تایید کد: " + e.getMessage());
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
                redirectAttributes.addFlashAttribute("error", "رمز عبور و تایید رمز عبور مطابقت ندارند");
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/password-reset/reset";
            }

            String passwordError = PasswordValidationUtil.validate(newPassword);
            if (passwordError != null) {
                redirectAttributes.addFlashAttribute("error", passwordError);
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/password-reset/reset";
            }

            var userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "کاربر یافت نشد");
                return "redirect:/password-reset/forgot";
            }

            var user = userOptional.get();

            User tempUser = new User();
            tempUser.setPassword(newPassword);
            userService.updateUser(user.getId(), tempUser);

            redirectAttributes.addFlashAttribute("success", 
                "رمز عبور شما با موفقیت تغییر کرد. اکنون می‌توانید وارد سیستم شوید.");
            return "redirect:/login";
        } catch (Exception e) {
            log.error("Error occurred while changing the password", e);
            redirectAttributes.addFlashAttribute("error", 
                "خطا در تغییر رمز عبور: " + e.getMessage());
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/password-reset/reset";
        }
    }
}

