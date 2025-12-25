package ir.maktabsharif.onlineexam.service;
import ir.maktabsharif.onlineexam.model.entity.Course;
import ir.maktabsharif.onlineexam.model.entity.Exam;
import ir.maktabsharif.onlineexam.model.entity.User;
import java.util.List;

public interface ExamService {
    Exam createExam(Exam exam);
    Exam updateExam(Long examId, Exam exam);
    void deleteExam(Long examId);
    Exam findById(Long id);
    List<Exam> getExamsByCourse(Course course);
    boolean isExamOwner(Long examId, Long teacherId);
}

