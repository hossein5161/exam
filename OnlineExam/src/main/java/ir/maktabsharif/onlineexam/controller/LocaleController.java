package ir.maktabsharif.onlineexam.controller;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LocaleController {

    @GetMapping("/change-locale")
    public String changeLocale(@RequestParam String lang, 
                               HttpServletRequest request) {

        String referer = request.getHeader("Referer");
        if (referer == null || referer.isEmpty()) {
            referer = "/dashboard";
        } else {
            int queryIndex = referer.indexOf('?');
            if (queryIndex > 0) {
                referer = referer.substring(0, queryIndex);
            }
            if (referer.startsWith("http://") || referer.startsWith("https://")) {
                int pathIndex = referer.indexOf('/', 8);
                if (pathIndex > 0) {
                    referer = referer.substring(pathIndex);
                }
            }
            if (referer.isEmpty() || referer.equals("/change-locale")) {
                referer = "/dashboard";
            }
        }
        
        return "redirect:" + referer;
    }
}

