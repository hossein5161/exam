package ir.maktabsharif.onlineexam.service;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.model.helper.UserUpdateChanges;
import java.util.Locale;

public interface EmailService {
    void sendUserApprovalEmail(User user, Locale locale);
    void sendUserRejectionEmail(User user, String rejectionReason, Locale locale);
    void sendPasswordResetCode(String email, String code, Locale locale);
    void sendUserUpdateEmail(User user, UserUpdateChanges changes, Locale locale);
    void sendUserDeletionEmail(User user, String deletionReason, Locale locale);
}

