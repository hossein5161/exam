package ir.maktabsharif.onlineexam.util;

import org.springframework.context.MessageSource;

import java.util.Locale;

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

    public static String validate(String password, MessageSource messageSource, Locale locale) {
        if (password == null || password.isEmpty()) {
            return messageSource.getMessage("password.validation.required", null, locale);
        }
        if (!password.equals(password.trim())) {
            return messageSource.getMessage("password.validation.trim", null, locale);
        }
        if (password.length() < 8) {
            return messageSource.getMessage("password.validation.minlength", null, locale);
        }
        if (!password.matches(".*[A-Z].*")) {
            return messageSource.getMessage("password.validation.uppercase", null, locale);
        }
        if (!password.matches(".*[a-z].*")) {
            return messageSource.getMessage("password.validation.lowercase", null, locale);
        }
        if (!password.matches(".*\\d.*")) {
            return messageSource.getMessage("password.validation.digit", null, locale);
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) {
            return messageSource.getMessage("password.validation.special", null, locale);
        }
        return null;
    }
    


    public static String validateOptional(String password, MessageSource messageSource, Locale locale) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        return validate(password, messageSource, locale);
    }
}

