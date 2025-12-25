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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    @Override
    @Transactional
    public User register(User user, String roleName) {

        Optional<User> existingUserByUsername = userRepository.findByUsername(user.getUsername());
        User userToDelete = null;
        
        if (existingUserByUsername.isPresent()) {
            User existingUser = existingUserByUsername.get();
            if (existingUser.getStatus() == UserStatus.APPROVED || existingUser.getStatus() == UserStatus.PENDING) {
                Locale locale = LocaleContextHolder.getLocale();
                String message = messageSource.getMessage("register.user.exists.add.role", null, locale);
                throw new RuntimeException(message);
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
                    Locale locale = LocaleContextHolder.getLocale();
                    String message = messageSource.getMessage("register.user.exists.add.role", null, locale);
                    throw new RuntimeException(message);
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
        Locale locale = LocaleContextHolder.getLocale();
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("error.role.not.found", 
                        new Object[]{roleName}, locale);
                    return new RuntimeException(message);
                });
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

        Locale locale = LocaleContextHolder.getLocale();
        UserUpdateChanges changes = UserUpdateChanges.builder().build();

        if (updatedUser.getFirstName() != null && !updatedUser.getFirstName().equals(user.getFirstName())) {
            String firstNameLabel = messageSource.getMessage("users.firstName", null, locale);
            changes.addChange(firstNameLabel, user.getFirstName(), updatedUser.getFirstName());
            user.setFirstName(updatedUser.getFirstName());
        }

        if (updatedUser.getLastName() != null && !updatedUser.getLastName().equals(user.getLastName())) {
            String lastNameLabel = messageSource.getMessage("users.lastName", null, locale);
            changes.addChange(lastNameLabel, user.getLastName(), updatedUser.getLastName());
            user.setLastName(updatedUser.getLastName());
        }

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            String passwordLabel = messageSource.getMessage("register.password", null, locale);
            String changedLabel = messageSource.getMessage("common.changed", null, locale);
            changes.addChange(passwordLabel, "***", changedLabel);
            changes.setPasswordChanged(true);
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        userRepository.save(user);
        return changes;
    }

    @Override
    @Transactional
    public User changeUserRole(Long userId, String roleName) {
        return changeUserRoles(userId, List.of(roleName));
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
        Locale locale = LocaleContextHolder.getLocale();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("error.user.not.found", null, locale);
                    return new RuntimeException(message);
                });

        if (currentUserId != null && currentUserId.equals(userId)) {
            String message = messageSource.getMessage("users.delete.error.cannot.delete.self", null, locale);
            throw new RuntimeException(message);
        }

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("error.role.admin.not.found", null, locale);
                    return new RuntimeException(message);
                });

        if (user.getRoles().contains(adminRole)) {
            List<User> allAdmins = userRepository.findByRolesContaining(adminRole);
            long remainingAdmins = allAdmins.stream()
                    .filter(admin -> !admin.getId().equals(userId))
                    .filter(admin -> admin.getStatus() == UserStatus.APPROVED)
                    .count();

            if (remainingAdmins == 0) {
                String message = messageSource.getMessage("users.delete.error.last.admin", null, locale);
                throw new RuntimeException(message);
            }
        }

        Role teacherRole = roleRepository.findByName("ROLE_TEACHER")
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("error.role.teacher.not.found", null, locale);
                    return new RuntimeException(message);
                });

        if (user.getRoles().contains(teacherRole)) {
            var courses = courseRepository.findByTeacher(user);
            if (!courses.isEmpty()) {
                StringBuilder courseList = new StringBuilder();
                for (int i = 0; i < courses.size(); i++) {
                    if (i > 0) {
                        courseList.append(", ");
                    }
                    courseList.append("\"").append(courses.get(i).getTitle()).append("\"");
                }
                String message = messageSource.getMessage("users.delete.error.teacher.has.courses", 
                    new Object[]{courses.size(), courseList.toString()}, locale);
                throw new RuntimeException(message);
            }
        }

        var studentCourses = courseRepository.findByStudentsContaining(user);
        if (!studentCourses.isEmpty()) {
            StringBuilder courseList = new StringBuilder();
            for (int i = 0; i < studentCourses.size(); i++) {
                if (i > 0) {
                    courseList.append(", ");
                }
                courseList.append("\"").append(studentCourses.get(i).getTitle()).append("\"");
            }
            String message = messageSource.getMessage("users.delete.error.student.has.courses", 
                new Object[]{studentCourses.size(), courseList.toString()}, locale);
            throw new RuntimeException(message);
        }

        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public User addRoleToExistingUser(String usernameOrEmail, String password, String roleName) {
        Locale locale = LocaleContextHolder.getLocale();

        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        }

        if (userOpt.isEmpty()) {
            String message = messageSource.getMessage("register.add.role.user.not.found", null, locale);
            throw new RuntimeException(message);
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            String message = messageSource.getMessage("register.add.role.invalid.password", null, locale);
            throw new RuntimeException(message);
        }

        if (user.getStatus() != UserStatus.APPROVED && user.getStatus() != UserStatus.PENDING) {
            String message = messageSource.getMessage("register.add.role.user.not.approved", null, locale);
            throw new RuntimeException(message);
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("error.role.not.found",
                            new Object[]{roleName}, locale);
                    return new RuntimeException(message);
                });

        if (user.getRoles().contains(role)) {
            String message = messageSource.getMessage("register.add.role.already.has",
                    new Object[]{roleName}, locale);
            throw new RuntimeException(message);
        }

        Set<Role> roles = new HashSet<>(user.getRoles());
        roles.add(role);
        user.setRoles(roles);
        user.setStatus(UserStatus.PENDING);
        user.setRejectionReason(null);

        return userRepository.save(user);
    }


    @Override
    @Transactional
    public User changeUserRoles(Long userId, List<String> roleNames) {
        Locale locale = LocaleContextHolder.getLocale();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("error.user.not.found", null, locale);
                    return new RuntimeException(message);
                });

        if (roleNames == null || roleNames.isEmpty()) {
            String message = messageSource.getMessage("users.roles.change.error.empty", null, locale);
            throw new RuntimeException(message);
        }

        Set<Role> newRoles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> {
                        String message = messageSource.getMessage("error.role.not.found",
                                new Object[]{roleName}, locale);
                        return new RuntimeException(message);
                    });
            newRoles.add(role);
        }

        var teacherCourses = courseRepository.findByTeacher(user);
        var studentCourses = courseRepository.findByStudentsContaining(user);

        Set<Role> currentRoles = user.getRoles();
        boolean wasTeacher = currentRoles.stream().anyMatch(r -> r.getName().equals("ROLE_TEACHER"));
        boolean wasStudent = currentRoles.stream().anyMatch(r -> r.getName().equals("ROLE_STUDENT"));

        boolean willBeTeacher = newRoles.stream().anyMatch(r -> r.getName().equals("ROLE_TEACHER"));
        boolean willBeStudent = newRoles.stream().anyMatch(r -> r.getName().equals("ROLE_STUDENT"));

        if (wasTeacher && !willBeTeacher && !teacherCourses.isEmpty()) {
            StringBuilder courseTitles = new StringBuilder();
            for (int i = 0; i < teacherCourses.size(); i++) {
                if (i > 0) courseTitles.append(", ");
                courseTitles.append("\"").append(teacherCourses.get(i).getTitle()).append("\"");
            }
            String message = messageSource.getMessage("users.role.change.error.teacher.has.courses",
                    new Object[]{courseTitles.toString()}, locale);
            throw new RuntimeException(message);
        }

        if (wasStudent && !willBeStudent && !studentCourses.isEmpty()) {
            StringBuilder courseTitles = new StringBuilder();
            for (int i = 0; i < studentCourses.size(); i++) {
                if (i > 0) courseTitles.append(", ");
                courseTitles.append("\"").append(studentCourses.get(i).getTitle()).append("\"");
            }
            String message = messageSource.getMessage("users.role.change.error.student.has.courses",
                    new Object[]{courseTitles.toString()}, locale);
            throw new RuntimeException(message);
        }

        user.setRoles(newRoles);
        return userRepository.save(user);
    }
}

