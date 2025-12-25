package ir.maktabsharif.onlineexam.config;
import ir.maktabsharif.onlineexam.util.locale.RequestLocaleHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class LanguageInterceptor implements HandlerInterceptor {
    
    private final ObjectProvider<RequestLocaleHolder> holderProvider;
    private final LocaleResolver localeResolver;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        RequestLocaleHolder holder = holderProvider.getObject();

        Locale locale = localeResolver.resolveLocale(request);
        
        log.debug("LanguageInterceptor: Resolved locale: {}", locale);
        
        LocaleContextHolder.setLocale(locale);
        
        holder.set(locale);
        
        return true;
    }
}

