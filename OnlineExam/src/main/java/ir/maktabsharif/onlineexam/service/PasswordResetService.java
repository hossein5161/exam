package ir.maktabsharif.onlineexam.service;

public interface PasswordResetService {
    String generateAndStoreResetCode(String email);
    boolean validateResetCode(String email, String code);
    void deleteResetCode(String email);
}

