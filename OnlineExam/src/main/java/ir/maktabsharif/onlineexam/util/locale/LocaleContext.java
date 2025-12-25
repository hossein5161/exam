package ir.maktabsharif.onlineexam.util.locale;
import org.springframework.beans.factory.ObjectProvider;
import java.util.Locale;

public class LocaleContext {
    private static ObjectProvider<RequestLocaleHolder> provider;

    public static void init(ObjectProvider<RequestLocaleHolder> p) {
        provider = p;
    }

    public static Locale getLocale() {
        try {
            RequestLocaleHolder holder = provider.getIfAvailable();
            if (holder != null)
                return holder.get();
        } catch (Exception ignored) {
        }
        return Locale.forLanguageTag("en");
    }
}

