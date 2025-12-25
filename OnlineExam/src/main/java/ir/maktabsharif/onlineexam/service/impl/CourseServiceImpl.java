package ir.maktabsharif.onlineexam.service.impl;
import ir.maktabsharif.onlineexam.model.entity.Course;
import ir.maktabsharif.onlineexam.model.entity.Role;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.repository.CourseRepository;
import ir.maktabsharif.onlineexam.repository.RoleRepository;
import ir.maktabsharif.onlineexam.repository.UserRepository;
import ir.maktabsharif.onlineexam.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MessageSource messageSource;

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
        Locale locale = LocaleContextHolder.getLocale();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("error.course.not.found", null, locale);
                    return new RuntimeException(message);
                });

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("error.user.not.found", null, locale);
                    return new RuntimeException(message);
                });

        Role teacherRole = roleRepository.findByName("ROLE_TEACHER")
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("error.role.teacher.not.found", null, locale);
                    return new RuntimeException(message);
                });

        if (!teacher.getRoles().contains(teacherRole)) {
            String message = messageSource.getMessage("courses.assign.teacher.error.not.teacher", null, locale);
            throw new RuntimeException(message);
        }

        if (course.getStudents().contains(teacher)) {
            String message = messageSource.getMessage("courses.assign.teacher.error.already.student", 
                new Object[]{teacher.getFirstName() + " " + teacher.getLastName(), course.getTitle()}, locale);
            throw new RuntimeException(message);
        }

        course.setTeacher(teacher);
        return courseRepository.save(course);
    }

    @Override
    @Transactional
    public Course addStudentToCourse(Long courseId, Long studentId) {
        Locale locale = LocaleContextHolder.getLocale();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("error.course.not.found", null, locale);
                    return new RuntimeException(message);
                });

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("error.user.not.found", null, locale);
                    return new RuntimeException(message);
                });

        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseThrow(() -> {
                    String message = messageSource.getMessage("error.role.student.not.found", null, locale);
                    return new RuntimeException(message);
                });

        if (!student.getRoles().contains(studentRole)) {
            String message = messageSource.getMessage("courses.add.student.error.not.student", null, locale);
            throw new RuntimeException(message);
        }

        if (course.getTeacher() != null && course.getTeacher().getId().equals(student.getId())) {
            String message = messageSource.getMessage("courses.add.student.error.already.teacher", 
                new Object[]{student.getFirstName() + " " + student.getLastName(), course.getTitle()}, locale);
            throw new RuntimeException(message);
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

