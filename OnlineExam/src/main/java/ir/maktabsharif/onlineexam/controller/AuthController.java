package ir.maktabsharif.onlineexam.controller;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.model.enums.UserStatus;
import ir.maktabsharif.onlineexam.service.UserService;
import ir.maktabsharif.onlineexam.service.impl.UserServiceImpl;
import ir.maktabsharif.onlineexam.util.PasswordValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final MessageSource messageSource;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                           @RequestParam(required = false) String logout,
                           Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
            && !authentication.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        
        if (error != null) {
            String errorMessage = messageSource.getMessage(
                "login.error", 
                null, 
                LocaleContextHolder.getLocale()
            );
            model.addAttribute("error", errorMessage);
        }
        if (logout != null) {
            String logoutMessage = messageSource.getMessage(
                "login.logout.success", 
                null, 
                LocaleContextHolder.getLocale()
            );
            model.addAttribute("message", logoutMessage);
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
            && !authentication.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                          @RequestParam String role,
                          RedirectAttributes redirectAttributes) {
        try {
            String passwordError = PasswordValidationUtil.validate(
                user.getPassword(), 
                messageSource, 
                LocaleContextHolder.getLocale()
            );
            if (passwordError != null) {
                redirectAttributes.addFlashAttribute("error", passwordError);
                return "redirect:/register";
            }
            
            UserServiceImpl userServiceImpl = (UserServiceImpl) userService;
            java.util.Optional<User> existingUserOpt = userServiceImpl.findUserByUsernameOrEmailOptional(user.getUsername());
            if (existingUserOpt.isEmpty()) {
                existingUserOpt = userServiceImpl.findUserByUsernameOrEmailOptional(user.getEmail());
            }
            
            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();
                if (existingUser.getStatus() == UserStatus.APPROVED
                    || existingUser.getStatus() == UserStatus.PENDING) {
                    redirectAttributes.addFlashAttribute("username", user.getUsername());
                    redirectAttributes.addFlashAttribute("email", user.getEmail());
                    Locale locale = LocaleContextHolder.getLocale();
                    String errorMessage = messageSource.getMessage("register.user.exists.add.role", null, locale);
                    redirectAttributes.addFlashAttribute("error", errorMessage);
                    return "redirect:/add-role";
                }
            }
            
            String roleName = "ROLE_" + role.toUpperCase();
            userService.register(user, roleName);
            String successMessage = messageSource.getMessage(
                "register.success", 
                null, 
                LocaleContextHolder.getLocale()
            );
            redirectAttributes.addFlashAttribute("success", successMessage);
            return "redirect:/login";
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            Locale locale = LocaleContextHolder.getLocale();
            String addRoleMessage = messageSource.getMessage("register.user.exists.add.role", null, locale);
            
            if (errorMessage != null && errorMessage.contains(addRoleMessage)) {
                redirectAttributes.addFlashAttribute("username", user.getUsername());
                redirectAttributes.addFlashAttribute("email", user.getEmail());
                redirectAttributes.addFlashAttribute("error", errorMessage);
                return "redirect:/add-role";
            }
            
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/register";
        }
    }


    @GetMapping("/add-role")
    public String addRolePage(@RequestParam(required = false) String username,
                              @RequestParam(required = false) String email,
                              Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
            && !authentication.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        
        if (username != null && !username.isEmpty()) {
            model.addAttribute("usernameOrEmail", username);
        } else if (email != null && !email.isEmpty()) {
            model.addAttribute("usernameOrEmail", email);
        }

        return "add_role";
    }

    @PostMapping("/add-role")
    public String addRole(@RequestParam String usernameOrEmail,
                         @RequestParam String password,
                         @RequestParam String role,
                         Model model) {
        try {
            String roleName = "ROLE_" + role.toUpperCase();
            model.addAttribute("usernameOrEmail", usernameOrEmail);

            UserServiceImpl userServiceImpl = (UserServiceImpl) userService;
            java.util.Optional<User> userOpt = userServiceImpl.findUserByUsernameOrEmailOptional(usernameOrEmail);
            if (userOpt.isEmpty()) {
                String message = messageSource.getMessage(
                    "register.add.role.user.not.found",
                    null,
                    LocaleContextHolder.getLocale()
                );
                model.addAttribute("error", message);
                return "add_role";
            }

            User existingUser = userOpt.get();
            if (passwordEncoder.matches(password, existingUser.getPassword())) {
                model.addAttribute("existingUser", existingUser);
                model.addAttribute("existingRoles", existingUser.getRoles());
            }

            User updatedUser = userService.addRoleToExistingUser(usernameOrEmail, password, roleName);
            model.addAttribute("existingUser", updatedUser);
            model.addAttribute("existingRoles", updatedUser.getRoles());
            
            Locale locale = LocaleContextHolder.getLocale();
            String successMessage = messageSource.getMessage(
                "register.add.role.success", 
                null, 
                locale
            );
            model.addAttribute("success", successMessage);
            return "add_role";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "add_role";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}

