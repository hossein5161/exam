package ir.maktabsharif.onlineexam.config;
import ir.maktabsharif.onlineexam.model.entity.Role;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.model.enums.UserStatus;
import ir.maktabsharif.onlineexam.repository.RoleRepository;
import ir.maktabsharif.onlineexam.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));
            
            Role teacherRole = roleRepository.findByName("ROLE_TEACHER")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_TEACHER").build()));
            
            Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_STUDENT").build()));

            if (userRepository.findByUsername("admin").isEmpty()) {
                userRepository.save(User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .firstName("حسین")
                        .lastName("محمدزاده")
                        .email("admin@gmail.com")
                        .status(UserStatus.APPROVED)
                        .roles(Set.of(adminRole))
                        .build());
            }
        };
    }
}

