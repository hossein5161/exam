package ir.maktabsharif.onlineexam.service;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.model.helper.UserUpdateChanges;

public interface EmailService {
    void sendUserApprovalEmail(User user);
    void sendUserRejectionEmail(User user, String rejectionReason);
    void sendPasswordResetCode(String email, String code);
    void sendUserUpdateEmail(User user, UserUpdateChanges changes);
}

