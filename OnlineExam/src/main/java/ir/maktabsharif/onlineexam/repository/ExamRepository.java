package ir.maktabsharif.onlineexam.repository;
import ir.maktabsharif.onlineexam.model.entity.Course;
import ir.maktabsharif.onlineexam.model.entity.Exam;
import ir.maktabsharif.onlineexam.model.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    List<Exam> findByTeacher(User teacher);
    List<Exam> findByCourseAndTeacher(Course course, User teacher);
    
    @EntityGraph(attributePaths = {"course", "teacher"})
    @Override
    Optional<Exam> findById(Long id);
    
    @EntityGraph(attributePaths = {"course", "teacher"})
    List<Exam> findByCourseOrderByIdDesc(Course course);
}

