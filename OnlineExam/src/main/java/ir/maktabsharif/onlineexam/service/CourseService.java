package ir.maktabsharif.onlineexam.service;
import ir.maktabsharif.onlineexam.model.entity.Course;
import ir.maktabsharif.onlineexam.model.entity.User;
import java.util.List;

public interface CourseService {
    Course createCourse(Course course);
    Course updateCourse(Long courseId, Course course);
    void deleteCourse(Long courseId);
    Course findById(Long id);
    List<Course> getAllCourses();
    Course assignTeacherToCourse(Long courseId, Long teacherId);
    Course addStudentToCourse(Long courseId, Long studentId);
    void removeStudentFromCourse(Long courseId, Long studentId);
    List<User> getCourseParticipants(Long courseId);
}

