package ir.maktabsharif.onlineexam.service;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.model.enums.UserStatus;
import ir.maktabsharif.onlineexam.model.helper.UserUpdateChanges;
import java.util.List;

public interface UserService {
    User register(User user, String roleName);
    User approveUser(Long userId);
    User rejectUser(Long userId, String rejectionReason);
    User updateUser(Long userId, User user);
    UserUpdateChanges updateUserWithChanges(Long userId, User updatedUser);
    User changeUserRole(Long userId, String roleName);
    List<User> searchUsers(String roleName, String firstName, String lastName, UserStatus status);
    List<User> getAllUsers();
    User findById(Long id);
    User findByUsername(String username);
    void deleteUser(Long userId, Long currentUserId);
}

