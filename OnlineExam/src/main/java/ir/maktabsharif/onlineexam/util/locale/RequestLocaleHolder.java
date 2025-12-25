package ir.maktabsharif.onlineexam.util.locale;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import java.util.Locale;

@Component
@RequestScope
public class RequestLocaleHolder {
    private Locale locale;

    public Locale get() {
        return locale == null ? Locale.forLanguageTag("en") : locale;
    }

    public void set(Locale locale) {
        this.locale = locale;
    }
}

