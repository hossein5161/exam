package ir.maktabsharif.onlineexam.service.impl;
import ir.maktabsharif.onlineexam.model.entity.Role;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.model.enums.UserStatus;
import ir.maktabsharif.onlineexam.model.helper.UserUpdateChanges;
import ir.maktabsharif.onlineexam.repository.CourseRepository;
import ir.maktabsharif.onlineexam.repository.RoleRepository;
import ir.maktabsharif.onlineexam.repository.UserRepository;
import ir.maktabsharif.onlineexam.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(User user, String roleName) {

        Optional<User> existingUserByUsername = userRepository.findByUsername(user.getUsername());
        User userToDelete = null;
        
        if (existingUserByUsername.isPresent()) {
            User existingUser = existingUserByUsername.get();
            if (existingUser.getStatus() == UserStatus.APPROVED || existingUser.getStatus() == UserStatus.PENDING) {
                throw new RuntimeException("نام کاربری قبلاً استفاده شده است");
            }
            if (existingUser.getStatus() == UserStatus.REJECTED) {
                userToDelete = existingUser;
            }
        }

        Optional<User> existingUserByEmail = userRepository.findByEmail(user.getEmail());

        if (existingUserByEmail.isPresent()) {
            User existingUser = existingUserByEmail.get();
            if (userToDelete == null || !userToDelete.getId().equals(existingUser.getId())) {
                if (existingUser.getStatus() == UserStatus.APPROVED || existingUser.getStatus() == UserStatus.PENDING) {
                    throw new RuntimeException("ایمیل قبلاً استفاده شده است");
                }
                if (existingUser.getStatus() == UserStatus.REJECTED) {
                    userToDelete = existingUser;
                }
            }
        }
        if (userToDelete != null) {
            userRepository.delete(userToDelete);
            userRepository.flush();
        }
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("نقش یافت نشد: " + roleName));
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setEmail(user.getEmail());
        newUser.setFirstName(user.getFirstName());
        newUser.setLastName(user.getLastName());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setStatus(UserStatus.PENDING);
        newUser.setRejectionReason(null);
        newUser.setRoles(Set.of(role));
        return userRepository.save(newUser);
    }

    @Override
    @Transactional
    public User approveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.APPROVED);
        user.setRejectionReason(null);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User rejectUser(Long userId, String rejectionReason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.REJECTED);
        user.setRejectionReason(rejectionReason);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long userId, User updatedUser) {
        UserUpdateChanges changes = updateUserWithChanges(userId, updatedUser);
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    @Transactional
    public UserUpdateChanges updateUserWithChanges(Long userId, User updatedUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserUpdateChanges changes = UserUpdateChanges.builder().build();

        if (updatedUser.getFirstName() != null && !updatedUser.getFirstName().equals(user.getFirstName())) {
            changes.addChange("نام", user.getFirstName(), updatedUser.getFirstName());
            user.setFirstName(updatedUser.getFirstName());
        }

        if (updatedUser.getLastName() != null && !updatedUser.getLastName().equals(user.getLastName())) {
            changes.addChange("نام خانوادگی", user.getLastName(), updatedUser.getLastName());
            user.setLastName(updatedUser.getLastName());
        }

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            changes.addChange("رمز عبور", "***", "تغییر یافته");
            changes.setPasswordChanged(true);
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        userRepository.save(user);
        return changes;
    }

    @Override
    @Transactional
    public User changeUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role newRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        var teacherCourses = courseRepository.findByTeacher(user);
        var studentCourses = courseRepository.findByStudentsContaining(user);
        
        if (!teacherCourses.isEmpty() || !studentCourses.isEmpty()) {
            StringBuilder message = new StringBuilder("امکان تغییر نقش این کاربر وجود ندارد.\n\n");
            
            if (!teacherCourses.isEmpty()) {
                message.append("این کاربر استاد دوره‌های زیر است:\n");
                for (int i = 0; i < teacherCourses.size(); i++) {
                    if (i > 0) message.append("، ");
                    message.append("\"").append(teacherCourses.get(i).getTitle()).append("\"");
                }
                message.append("\n");
            }
            
            if (!studentCourses.isEmpty()) {
                message.append("این کاربر دانشجوی دوره‌های زیر است:\n");
                for (int i = 0; i < studentCourses.size(); i++) {
                    if (i > 0) message.append("، ");
                    message.append("\"").append(studentCourses.get(i).getTitle()).append("\"");
                }
                message.append("\n");
            }
            
            message.append("\nبرای تغییر نقش، لطفاً ابتدا این کاربر را از دوره‌های فوق حذف کنید.");
            throw new RuntimeException(message.toString());
        }

        Set<Role> roles = new HashSet<>();
        roles.add(newRole);
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Override
    public List<User> searchUsers(String roleName, String firstName, String lastName, UserStatus status) {
        Integer statusOrdinal = status != null ? status.ordinal() : null;
        return userRepository.searchUsers(roleName, firstName, lastName, statusOrdinal);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    @Override
    @Transactional
    public void deleteUser(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد"));

        if (currentUserId != null && currentUserId.equals(userId)) {
            throw new RuntimeException("شما نمی‌توانید خودتان را حذف کنید");
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new RuntimeException("نقش ادمین یافت نشد"));

        if (user.getRoles().contains(adminRole)) {
            List<User> allAdmins = userRepository.findByRolesContaining(adminRole);
            long remainingAdmins = allAdmins.stream()
                    .filter(admin -> !admin.getId().equals(userId))
                    .filter(admin -> admin.getStatus() == UserStatus.APPROVED)
                    .count();

            if (remainingAdmins == 0) {
                throw new RuntimeException(
                    "امکان حذف این کاربر وجود ندارد. این کاربر ادمین است و حذف آن باعث می‌شود که هیچ ادمینی در سیستم باقی نماند. " +
                    "لطفاً ابتدا یک ادمین دیگر ایجاد کنید و سپس اقدام به حذف این کاربر نمایید."
                );
            }
        }

        Role teacherRole = roleRepository.findByName("ROLE_TEACHER")
                .orElseThrow(() -> new RuntimeException("نقش استاد یافت نشد"));

        if (user.getRoles().contains(teacherRole)) {
            var courses = courseRepository.findByTeacher(user);
            if (!courses.isEmpty()) {
                StringBuilder courseList = new StringBuilder();
                for (int i = 0; i < courses.size(); i++) {
                    if (i > 0) {
                        courseList.append("، ");
                    }
                    courseList.append("\"").append(courses.get(i).getTitle()).append("\"");
                }
                throw new RuntimeException(
                    "امکان حذف این کاربر وجود ندارد. این کاربر استاد است و در حال حاضر استاد " + 
                    courses.size() + " دوره می‌باشد:\n" + 
                    courseList + "\n\n" +
                    "برای حذف این کاربر، لطفاً ابتدا استاد را از تمام دوره‌هایش حذف کنید و سپس مجدداً اقدام به حذف کاربر نمایید."
                );
            }
        }

        var studentCourses = courseRepository.findByStudentsContaining(user);
        if (!studentCourses.isEmpty()) {
            StringBuilder courseList = new StringBuilder();
            for (int i = 0; i < studentCourses.size(); i++) {
                if (i > 0) {
                    courseList.append("، ");
                }
                courseList.append("\"").append(studentCourses.get(i).getTitle()).append("\"");
            }
            throw new RuntimeException(
                "امکان حذف این کاربر وجود ندارد. این کاربر دانشجو است و در حال حاضر در " + 
                studentCourses.size() + " دوره ثبت‌نام کرده است:\n" + 
                courseList + "\n\n" +
                "برای حذف این کاربر، لطفاً ابتدا دانشجو را از تمام دوره‌هایش حذف کنید و سپس مجدداً اقدام به حذف کاربر نمایید."
            );
        }

        userRepository.deleteById(userId);
    }
}

