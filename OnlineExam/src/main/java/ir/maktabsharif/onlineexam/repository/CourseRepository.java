package ir.maktabsharif.onlineexam.repository;
import ir.maktabsharif.onlineexam.model.entity.Course;
import ir.maktabsharif.onlineexam.model.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    List<Course> findByTeacher(User teacher);
    List<Course> findByStudentsContaining(User student);
    
    @EntityGraph(attributePaths = {"students", "teacher"})
    @Override
    List<Course> findAll();
    
    @EntityGraph(attributePaths = {"students", "teacher"})
    @Override
    Optional<Course> findById(Long id);
}

