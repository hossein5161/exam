package ir.maktabsharif.onlineexam.service.impl;
import ir.maktabsharif.onlineexam.model.entity.Course;
import ir.maktabsharif.onlineexam.model.entity.Exam;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.repository.CourseRepository;
import ir.maktabsharif.onlineexam.repository.ExamRepository;
import ir.maktabsharif.onlineexam.repository.UserRepository;
import ir.maktabsharif.onlineexam.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Exam createExam(Exam exam) {
        if (exam.getCourse() == null || exam.getCourse().getId() == null) {
            throw new RuntimeException("Course is required");
        }
        if (exam.getTeacher() == null || exam.getTeacher().getId() == null) {
            throw new RuntimeException("Teacher is required");
        }
        
        Course course = courseRepository.findById(exam.getCourse().getId())
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        User teacher = userRepository.findById(exam.getTeacher().getId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        
        if (!course.getTeacher().getId().equals(teacher.getId())) {
            throw new RuntimeException("Teacher is not assigned to this course");
        }
        
        exam.setCourse(course);
        exam.setTeacher(teacher);
        
        return examRepository.save(exam);
    }

    @Override
    @Transactional
    public Exam updateExam(Long examId, Exam updatedExam) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        if (updatedExam.getTitle() != null) {
            exam.setTitle(updatedExam.getTitle());
        }
        if (updatedExam.getDescription() != null) {
            exam.setDescription(updatedExam.getDescription());
        }
        if (updatedExam.getDurationMinutes() != null) {
            exam.setDurationMinutes(updatedExam.getDurationMinutes());
        }

        return examRepository.save(exam);
    }

    @Override
    @Transactional
    public void deleteExam(Long examId) {
        if (!examRepository.existsById(examId)) {
            throw new RuntimeException("Exam not found");
        }
        examRepository.deleteById(examId);
    }

    @Override
    @Transactional(readOnly = true)
    public Exam findById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Exam> getExamsByCourse(Course course) {
        return examRepository.findByCourseOrderByIdDesc(course);
    }


    @Override
    @Transactional(readOnly = true)
    public boolean isExamOwner(Long examId, Long teacherId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        return exam.getTeacher().getId().equals(teacherId);
    }
}

