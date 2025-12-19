package ir.maktabsharif.onlineexam.util;

public class PasswordValidationUtil {

    public static String validate(String password) {
        if (password == null || password.isEmpty()) {
            return "رمز عبور الزامی است";
        }
        if (!password.equals(password.trim())) {
            return "رمز عبور نباید فاصله در ابتدا یا انتها داشته باشد";
        }
        if (password.length() < 8) {
            return "رمز عبور باید حداقل ۸ کاراکتر باشد";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "رمز عبور باید حداقل یک حرف بزرگ داشته باشد";
        }
        if (!password.matches(".*[a-z].*")) {
            return "رمز عبور باید حداقل یک حرف کوچک داشته باشد";
        }
        if (!password.matches(".*\\d.*")) {
            return "رمز عبور باید حداقل یک عدد داشته باشد";
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) {
            return "رمز عبور باید حداقل یک کاراکتر خاص داشته باشد";
        }
        return null;
    }
    
    public static String validateOptional(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        return validate(password);
    }
}

