package ir.maktabsharif.onlineexam.validation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]{8,}$"
    );

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            return true;
        }

        if (!password.equals(password.trim())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("رمز عبور نباید فاصله در ابتدا یا انتها داشته باشد")
                   .addConstraintViolation();
            return false;
        }
        
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}

