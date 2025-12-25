package ir.maktabsharif.onlineexam.repository;
import ir.maktabsharif.onlineexam.model.entity.Course;
import ir.maktabsharif.onlineexam.model.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseCode(String courseCode);
    List<Course> findByTeacher(User teacher);
    
    @EntityGraph(attributePaths = {"students", "teacher"})
    @Query("SELECT c FROM Course c WHERE c.teacher = :teacher")
    List<Course> findByTeacherWithStudents(@Param("teacher") User teacher);
    
    List<Course> findByStudentsContaining(User student);
    
    @EntityGraph(attributePaths = {"students", "teacher"})
    @Override
    List<Course> findAll();
    
    @EntityGraph(attributePaths = {"students", "teacher"})
    @Override
    Optional<Course> findById(Long id);
}

