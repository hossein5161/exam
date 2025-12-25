package ir.maktabsharif.onlineexam.controller;
import ir.maktabsharif.onlineexam.model.entity.Role;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.model.enums.UserStatus;
import ir.maktabsharif.onlineexam.service.EmailService;
import ir.maktabsharif.onlineexam.service.UserService;
import ir.maktabsharif.onlineexam.util.PasswordValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final EmailService emailService;
    private final MessageSource messageSource;
    private final UserDetailsService userDetailsService;

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
        Locale locale = LocaleContextHolder.getLocale();
        try {
            User user = userService.approveUser(id);
            String successMessage = messageSource.getMessage("users.approve.success.email", null, locale);
            redirectAttributes.addFlashAttribute("success", successMessage);

            try {
                emailService.sendUserApprovalEmail(user, locale);
                log.info("Approval email sent to user {}", user.getEmail());
            } catch (Exception e) {
                log.error("Error sending email to {}", user.getEmail(), e);
                String warningMessage = messageSource.getMessage("users.approve.success.email.error", 
                    new Object[]{e.getMessage()}, locale);
                redirectAttributes.addFlashAttribute("warning", warningMessage);
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
                            @RequestParam(required = false) String[] roleNames,
                            @RequestParam(required = false) String password,
                            RedirectAttributes redirectAttributes,
                            jakarta.servlet.http.HttpSession session) {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            User userBeforeUpdate = userService.findById(id);
            String notAssigned = messageSource.getMessage("common.not.assigned", null, locale);
            
            String oldRolesStr = userBeforeUpdate.getRoles().stream()
                    .map(Role::getPersianName)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(notAssigned);

            String newRolesStr = null;
            if (roleNames != null && roleNames.length > 0) {
                List<String> roleNameList = new ArrayList<>();
                for (String roleName : roleNames) {
                    if (roleName != null && !roleName.isEmpty()) {
                        roleNameList.add("ROLE_" + roleName.toUpperCase());
                    }
                }
                if (!roleNameList.isEmpty()) {
                    userService.changeUserRoles(id, roleNameList);
                    User userAfterRoleChange = userService.findById(id);
                    newRolesStr = userAfterRoleChange.getRoles().stream()
                            .map(Role::getPersianName)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse(notAssigned);
                    
                    Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
                    if (currentAuth != null && currentAuth.getName().equals(userAfterRoleChange.getUsername())) {
                        UserDetails updatedUserDetails = userDetailsService.loadUserByUsername(userAfterRoleChange.getUsername());
                        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                                updatedUserDetails,
                                currentAuth.getCredentials(),
                                updatedUserDetails.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(newAuth);
                        updatedUserDetails.getAuthorities().stream().findFirst().ifPresent(authority -> {
                            String roleNamePlain = authority.getAuthority().startsWith("ROLE_")
                                    ? authority.getAuthority().substring(5)
                                    : authority.getAuthority();
                            session.setAttribute("selectedRole", roleNamePlain);
                        });
                        log.info("Authentication refreshed for user {} after role change", userAfterRoleChange.getUsername());
                    }
                }
            }

            user.setRoles(null);
            
            if (password != null && !password.isEmpty()) {
                String passwordError = PasswordValidationUtil.validateOptional(
                    password, 
                    messageSource, 
                    LocaleContextHolder.getLocale()
                );
                if (passwordError != null) {
                    redirectAttributes.addFlashAttribute("error", passwordError);
                    return "redirect:/admin/users/" + id + "/edit";
                }
                user.setPassword(password);
            }

            var changes = userService.updateUserWithChanges(id, user);

            if (newRolesStr != null && !newRolesStr.equals(oldRolesStr)) {
                String roleLabel = messageSource.getMessage("common.roles", null, locale);
                changes.addChange(roleLabel, oldRolesStr, newRolesStr);
            }

            if (changes.hasChanges()) {
                try {
                    User updatedUser = userService.findById(id);
                    emailService.sendUserUpdateEmail(updatedUser, changes, locale);
                    log.info("Update email sent to user {}", updatedUser.getEmail());
                } catch (Exception e) {
                    User updatedUser = userService.findById(id);
                    log.error("Error sending update email to {}", updatedUser.getEmail(), e);
                    String warningMessage = messageSource.getMessage("users.update.success.email.error", 
                        new Object[]{e.getMessage()}, locale);
                    redirectAttributes.addFlashAttribute("warning", warningMessage);
                }
            }

            String successMessage = messageSource.getMessage("users.update.success", null, locale);
            redirectAttributes.addFlashAttribute("success", successMessage);
        } catch (Exception e) {
            log.error("An error occurred while updating the user", e);
            Locale locale = LocaleContextHolder.getLocale();
            String errorMessage = messageSource.getMessage("users.update.error", 
                new Object[]{e.getMessage()}, locale);
            redirectAttributes.addFlashAttribute("error", errorMessage);
        }
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        boolean isCurrentUser = currentAuth != null && currentAuth.getName().equals(user.getUsername());
        boolean isAdminNow = currentAuth != null && currentAuth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isCurrentUser && !isAdminNow) {
            return "redirect:/dashboard";
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/change-role")
    public String changeUserRole(@PathVariable Long id,
                                @RequestParam String roleName,
                                RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            userService.changeUserRole(id, "ROLE_" + roleName.toUpperCase());
            String successMessage = messageSource.getMessage("users.role.change.success", null, locale);
            redirectAttributes.addFlashAttribute("success", successMessage);
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
        Locale locale = LocaleContextHolder.getLocale();
        try {
            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                String errorMessage = messageSource.getMessage("users.reject.reason.required", null, locale);
                redirectAttributes.addFlashAttribute("error", errorMessage);
                return "redirect:/admin/users/" + id + "/reject";
            }
            
            User user = userService.rejectUser(id, rejectionReason.trim());
            String successMessage = messageSource.getMessage("users.reject.success.email", null, locale);
            redirectAttributes.addFlashAttribute("success", successMessage);

            try {
                emailService.sendUserRejectionEmail(user, rejectionReason.trim(), locale);
                log.info("Rejection email sent to user {}", user.getEmail());
            } catch (Exception e) {
                log.error("Error while sending rejection email to {}", user.getEmail(), e);
                String warningMessage = messageSource.getMessage("users.reject.success.email.error", 
                    new Object[]{e.getMessage()}, locale);
                redirectAttributes.addFlashAttribute("warning", warningMessage);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/delete")
    public String deleteUserPage(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "admin/delete_user";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                            @RequestParam String deletionReason,
                            RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userService.findByUsername(currentUsername);

            if (currentUser.getId().equals(id)) {
                String errorMessage = messageSource.getMessage("users.delete.error.cannot.delete.self", null, locale);
                redirectAttributes.addFlashAttribute("error", errorMessage);
                return "redirect:/admin/users";
            }

            if (deletionReason == null || deletionReason.trim().isEmpty()) {
                String errorMessage = messageSource.getMessage("users.delete.reason.required", null, locale);
                redirectAttributes.addFlashAttribute("error", errorMessage);
                return "redirect:/admin/users/" + id + "/delete";
            }

            User userToDelete = userService.findById(id);
            String userEmail = userToDelete.getEmail();

            try {
                emailService.sendUserDeletionEmail(userToDelete, deletionReason.trim(), locale);
                log.info("Deletion email sent to user {}", userEmail);
            } catch (Exception e) {
                log.error("Error sending deletion email to {}", userEmail, e);
                String warningMessage = messageSource.getMessage("users.delete.email.error", 
                    new Object[]{e.getMessage()}, locale);
                redirectAttributes.addFlashAttribute("warning", warningMessage);
                return "redirect:/admin/users/" + id + "/delete";
            }

            userService.deleteUser(id, currentUser.getId());
            String successMessage = messageSource.getMessage("users.delete.success.email", null, locale);
            redirectAttributes.addFlashAttribute("success", successMessage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}

