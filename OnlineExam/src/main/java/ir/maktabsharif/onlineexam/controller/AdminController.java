package ir.maktabsharif.onlineexam.controller;
import ir.maktabsharif.onlineexam.model.entity.Role;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.model.enums.UserStatus;
import ir.maktabsharif.onlineexam.service.EmailService;
import ir.maktabsharif.onlineexam.service.UserService;
import ir.maktabsharif.onlineexam.util.PasswordValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final EmailService emailService;

    @GetMapping("/users")
    public String usersPage(@RequestParam(required = false) String roleName,
                           @RequestParam(required = false) String firstName,
                           @RequestParam(required = false) String lastName,
                           @RequestParam(required = false) String status,
                           Model model) {
        UserStatus userStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                userStatus = UserStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
            }
        }
        List<User> users;
        if ((roleName != null && !roleName.isEmpty()) || 
            (firstName != null && !firstName.isEmpty()) || 
            (lastName != null && !lastName.isEmpty()) || 
            userStatus != null) {
            users = userService.searchUsers(roleName, firstName, lastName, userStatus);
        } else {
            users = userService.getAllUsers();
        }
        model.addAttribute("users", users);
        model.addAttribute("roleName", roleName);
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("status", status);
        return "admin/users";
    }

    @PostMapping("/users/{id}/approve")
    public String approveUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.approveUser(id);
            redirectAttributes.addFlashAttribute("success", "کاربر با موفقیت تایید شد و ایمیل ارسال شد");

            try {
                emailService.sendUserApprovalEmail(user);
                log.info("Approval email sent to user {}", user.getEmail());
            } catch (Exception e) {
                log.error("Error sending email to {}", user.getEmail(), e);
                redirectAttributes.addFlashAttribute("warning", 
                    "کاربر تایید شد اما خطا در ارسال ایمیل: " + e.getMessage());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String editUserPage(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "admin/edit_user";
    }

    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id,
                            @ModelAttribute User user,
                            @RequestParam(required = false) String roleName,
                            @RequestParam(required = false) String password,
                            RedirectAttributes redirectAttributes) {
        try {
            User userBeforeUpdate = userService.findById(id);
            String oldRoleName = userBeforeUpdate.getRoles().stream()
                    .findFirst()
                    .map(Role::getPersianName)
                    .orElse("تعیین نشده");

            String newRoleName = null;
            if (roleName != null && !roleName.isEmpty()) {
                userService.changeUserRole(id, "ROLE_" + roleName.toUpperCase());
                User userAfterRoleChange = userService.findById(id);
                newRoleName = userAfterRoleChange.getRoles().stream()
                        .findFirst()
                        .map(Role::getPersianName)
                        .orElse("تعیین نشده");
            }

            user.setRoles(null);
            
            if (password != null && !password.isEmpty()) {
                String passwordError = PasswordValidationUtil.validateOptional(password);
                if (passwordError != null) {
                    redirectAttributes.addFlashAttribute("error", passwordError);
                    return "redirect:/admin/users/" + id + "/edit";
                }
                user.setPassword(password);
            }

            var changes = userService.updateUserWithChanges(id, user);

            if (newRoleName != null && !newRoleName.equals(oldRoleName)) {
                changes.addChange("نقش", oldRoleName, newRoleName);
            }

            if (changes.hasChanges()) {
                try {
                    User updatedUser = userService.findById(id);
                    emailService.sendUserUpdateEmail(updatedUser, changes);
                    log.info("Update email sent to user {}", updatedUser.getEmail());
                } catch (Exception e) {
                    User updatedUser = userService.findById(id);
                    log.error("Error sending update email to {}", updatedUser.getEmail(), e);
                    redirectAttributes.addFlashAttribute("warning", 
                        "اطلاعات کاربر به‌روزرسانی شد اما خطا در ارسال ایمیل: " + e.getMessage());
                }
            }

            redirectAttributes.addFlashAttribute("success", "اطلاعات کاربر با موفقیت به‌روزرسانی شد");
        } catch (Exception e) {
            log.error("An error occurred while updating the user", e);
            redirectAttributes.addFlashAttribute("error", "خطا در به‌روزرسانی: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/change-role")
    public String changeUserRole(@PathVariable Long id,
                                @RequestParam String roleName,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.changeUserRole(id, "ROLE_" + roleName.toUpperCase());
            redirectAttributes.addFlashAttribute("success", "نقش کاربر با موفقیت تغییر کرد");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/reject")
    public String rejectUserPage(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "admin/reject_user";
    }

    @PostMapping("/users/{id}/reject")
    public String rejectUser(@PathVariable Long id,
                            @RequestParam String rejectionReason,
                            RedirectAttributes redirectAttributes) {
        try {
            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "لطفاً دلیل رد شدن را وارد کنید");
                return "redirect:/admin/users/" + id + "/reject";
            }
            
            User user = userService.rejectUser(id, rejectionReason.trim());
            redirectAttributes.addFlashAttribute("success", "کاربر با موفقیت رد شد و ایمیل ارسال شد");

            try {
                emailService.sendUserRejectionEmail(user, rejectionReason.trim());
                log.info("Rejection email sent to user {}", user.getEmail());
            } catch (Exception e) {
                log.error("Error while sending rejection email to {}", user.getEmail(), e);
                redirectAttributes.addFlashAttribute("warning", 
                    "کاربر رد شد اما خطا در ارسال ایمیل: " + e.getMessage());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userService.findByUsername(currentUsername);

            if (currentUser.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "شما نمی‌توانید خودتان را حذف کنید");
                return "redirect:/admin/users";
            }
            userService.deleteUser(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "کاربر با موفقیت حذف شد");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}

