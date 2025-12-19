package ir.maktabsharif.onlineexam.repository;
import ir.maktabsharif.onlineexam.model.entity.Role;
import ir.maktabsharif.onlineexam.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query(value = "SELECT DISTINCT u.* FROM users u " +
           "LEFT JOIN user_roles ur ON u.id = ur.user_id " +
           "LEFT JOIN roles r ON ur.role_id = r.id " +
           "WHERE (:roleName IS NULL OR :roleName = '' OR r.name = :roleName) " +
           "AND (:firstName IS NULL OR :firstName = '' OR u.first_name ILIKE CONCAT('%', :firstName, '%')) " +
           "AND (:lastName IS NULL OR :lastName = '' OR u.last_name ILIKE CONCAT('%', :lastName, '%')) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "ORDER BY u.id", nativeQuery = true)
    List<User> searchUsers(@Param("roleName") String roleName,
                          @Param("firstName") String firstName,
                          @Param("lastName") String lastName,
                          @Param("status") Integer status);
    
    List<User> findByRolesContaining(Role role);
}

