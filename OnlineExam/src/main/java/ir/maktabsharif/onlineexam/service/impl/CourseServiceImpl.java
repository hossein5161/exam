package ir.maktabsharif.onlineexam.service.impl;
import ir.maktabsharif.onlineexam.model.entity.Course;
import ir.maktabsharif.onlineexam.model.entity.Role;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.repository.CourseRepository;
import ir.maktabsharif.onlineexam.repository.RoleRepository;
import ir.maktabsharif.onlineexam.repository.UserRepository;
import ir.maktabsharif.onlineexam.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public Course createCourse(Course course) {
        if (courseRepository.findByCourseCode(course.getCourseCode()).isPresent()) {
            throw new RuntimeException("Course code already exists");
        }
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course updateCourse(Long courseId, Course updatedCourse) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (updatedCourse.getTitle() != null) {
            course.setTitle(updatedCourse.getTitle());
        }
        if (updatedCourse.getStartDate() != null) {
            course.setStartDate(updatedCourse.getStartDate());
        }
        if (updatedCourse.getEndDate() != null) {
            course.setEndDate(updatedCourse.getEndDate());
        }
        if (updatedCourse.getCourseCode() != null && !updatedCourse.getCourseCode().equals(course.getCourseCode())) {
            if (courseRepository.findByCourseCode(updatedCourse.getCourseCode()).isPresent()) {
                throw new RuntimeException("Course code already exists");
            }
            course.setCourseCode(updatedCourse.getCourseCode());
        }

        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        courseRepository.deleteById(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }


    @Override
    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    @Transactional
    public Course assignTeacherToCourse(Long courseId, Long teacherId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role teacherRole = roleRepository.findByName("ROLE_TEACHER")
                .orElseThrow(() -> new RuntimeException("Teacher role not found"));

        if (!teacher.getRoles().contains(teacherRole)) {
            throw new RuntimeException("User is not a teacher");
        }

        course.setTeacher(teacher);
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course addStudentToCourse(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseThrow(() -> new RuntimeException("Student role not found"));

        if (!student.getRoles().contains(studentRole)) {
            throw new RuntimeException("User is not a student");
        }

        if (!course.getStudents().contains(student)) {
            course.getStudents().add(student);
        }

        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public void removeStudentFromCourse(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        course.getStudents().remove(student);
        courseRepository.save(course);
    }



    @Override
    public List<User> getCourseParticipants(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        List<User> participants = new ArrayList<>();
        if (course.getTeacher() != null) {
            participants.add(course.getTeacher());
        }
        participants.addAll(course.getStudents());
        return participants;
    }
}

