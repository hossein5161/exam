package ir.maktabsharif.onlineexam.controller;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.service.UserService;
import ir.maktabsharif.onlineexam.util.PasswordValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                           @RequestParam(required = false) String logout,
                           Model model) {
        if (error != null) {
            model.addAttribute("error", "نام کاربری یا رمز عبور اشتباه است");
        }
        if (logout != null) {
            model.addAttribute("message", "با موفقیت خارج شدید");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                          @RequestParam String role,
                          RedirectAttributes redirectAttributes) {
        try {
            String passwordError = PasswordValidationUtil.validate(user.getPassword());
            if (passwordError != null) {
                redirectAttributes.addFlashAttribute("error", passwordError);
                return "redirect:/register";
            }
            
            String roleName = "ROLE_" + role.toUpperCase();
            userService.register(user, roleName);
            redirectAttributes.addFlashAttribute("success", 
                "ثبت نام با موفقیت انجام شد. منتظر تایید مدیر باشید.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}

