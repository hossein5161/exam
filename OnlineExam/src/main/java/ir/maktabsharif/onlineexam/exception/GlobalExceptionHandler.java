package ir.maktabsharif.onlineexam.exception;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException e, RedirectAttributes redirectAttributes) {
        String message = e.getMessage();
        
        try {
            if (message != null && message.contains(".")) {
                String localizedMessage = messageSource.getMessage(
                    message, 
                    null, 
                    message, 
                    LocaleContextHolder.getLocale()
                );
                if (!localizedMessage.equals(message)) {
                    message = localizedMessage;
                }
            }
        } catch (Exception ex) {
        }
        
        redirectAttributes.addFlashAttribute("error", message);
        return "redirect:/dashboard";
    }
}

