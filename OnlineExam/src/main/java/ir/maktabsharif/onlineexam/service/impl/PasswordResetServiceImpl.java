package ir.maktabsharif.onlineexam.service.impl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ir.maktabsharif.onlineexam.model.dto.PasswordResetCode;
import ir.maktabsharif.onlineexam.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String HASH_TABLE_NAME = "password_reset";
    private static final int CODE_EXPIRATION_MINUTES = 5;

    private static final ObjectMapper objectMapper;
    
    static {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public String generateAndStoreResetCode(String email) {
        deleteResetCode(email);

        String code = generateRandomCode();

        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime expiresAt = createdAt.plusMinutes(CODE_EXPIRATION_MINUTES);
        
        PasswordResetCode resetCode = new PasswordResetCode(code, createdAt, expiresAt);

        redisTemplate.opsForHash().put(HASH_TABLE_NAME, email, resetCode);

        log.info("Password reset code generated for {}: {}", email, code);
        return code;
    }

    @Override
    public boolean validateResetCode(String email, String code) {
        if (!redisTemplate.opsForHash().hasKey(HASH_TABLE_NAME, email)) {
            log.warn("Password reset code not found for {}", email);
            return false;
        }
        Object rawValue = redisTemplate.opsForHash().get(HASH_TABLE_NAME, email);
        if (rawValue == null) {
            log.warn("Password reset code not found for {}", email);
            return false;
        }
        PasswordResetCode resetCode = convertToPasswordResetCode(rawValue);
        if (resetCode == null) {
            log.warn("Failed to convert password reset code data for {}", email);
            return false;
        }
        if (isCodeExpired(resetCode)) {
            log.warn("Password reset code expired for {}", email);
            deleteResetCode(email);
            return false;
        }
        boolean isValid = code != null && code.equals(resetCode.getCode());
        if (isValid) {
            log.info("Password reset code is valid for {}", email);
        } else {
            log.warn("Invalid password reset code for {}", email);
        }
        return isValid;
    }

    @Override
    public void deleteResetCode(String email) {
        redisTemplate.opsForHash().delete(HASH_TABLE_NAME, email);
        log.info("Password reset code for {} has been deleted", email);
    }


    private boolean isCodeExpired(PasswordResetCode resetCode) {
        return LocalDateTime.now().isAfter(resetCode.getExpiresAt());
    }
    

    private PasswordResetCode convertToPasswordResetCode(Object rawValue) {
        try {
            if (rawValue instanceof PasswordResetCode) {
                return (PasswordResetCode) rawValue;
            }
            
            if (rawValue instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) rawValue;
                return objectMapper.convertValue(map, PasswordResetCode.class);
            }

            if (rawValue instanceof String) {
                return objectMapper.readValue((String) rawValue, PasswordResetCode.class);
            }

            log.error("Unexpected data type for PasswordResetCode: {}", rawValue.getClass().getName());
            return null;
        } catch (Exception e) {
            log.error("Error converting data to PasswordResetCode", e);
            return null;
        }
    }

    private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}

