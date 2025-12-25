package ir.maktabsharif.onlineexam.controller;
import ir.maktabsharif.onlineexam.model.entity.Course;
import ir.maktabsharif.onlineexam.model.entity.Role;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.model.enums.UserStatus;
import ir.maktabsharif.onlineexam.repository.RoleRepository;
import ir.maktabsharif.onlineexam.repository.UserRepository;
import ir.maktabsharif.onlineexam.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/courses")
public class CourseController {

    private final CourseService courseService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MessageSource messageSource;

    @GetMapping
    public String coursesPage(Model model) {
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        return "admin/courses";
    }

    @GetMapping("/new")
    public String newCoursePage(Model model) {
        model.addAttribute("course", new Course());
        return "admin/new_course";
    }

    @PostMapping
    public String createCourse(@ModelAttribute Course course, RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            courseService.createCourse(course);
            String successMessage = messageSource.getMessage("courses.create.success", null, locale);
            redirectAttributes.addFlashAttribute("success", successMessage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/courses";
    }

    @GetMapping("/{id}/edit")
    public String editCoursePage(@PathVariable Long id, Model model) {
        Course course = courseService.findById(id);
        model.addAttribute("course", course);
        return "admin/edit_course";
    }

    @PostMapping("/{id}/edit")
    public String updateCourse(@PathVariable Long id,
                              @ModelAttribute Course course,
                              RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            courseService.updateCourse(id, course);
            String successMessage = messageSource.getMessage("courses.update.success", null, locale);
            redirectAttributes.addFlashAttribute("success", successMessage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/courses/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            courseService.deleteCourse(id);
            String successMessage = messageSource.getMessage("courses.delete.success", null, locale);
            redirectAttributes.addFlashAttribute("success", successMessage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/courses";
    }

    @GetMapping("/{id}/assign-teacher")
    public String assignTeacherPage(@PathVariable Long id, Model model) {
        Course course = courseService.findById(id);
        Role teacherRole = roleRepository.findByName("ROLE_TEACHER")
                .orElseThrow(() -> new RuntimeException("Teacher role not found"));
        List<User> teachers = userRepository.findByRolesContaining(teacherRole);
        
        model.addAttribute("course", course);
        model.addAttribute("teachers", teachers);
        return "admin/assign_teacher";
    }

    @PostMapping("/{id}/assign-teacher")
    public String assignTeacher(@PathVariable Long id,
                               @RequestParam Long teacherId,
                               RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            courseService.assignTeacherToCourse(id, teacherId);
            String successMessage = messageSource.getMessage("courses.assign.teacher.success", null, locale);
            redirectAttributes.addFlashAttribute("success", successMessage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/courses/" + id;
    }

    @GetMapping("/{id}/add-student")
    public String addStudentPage(@PathVariable Long id, Model model) {
        Course course = courseService.findById(id);
        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseThrow(() -> new RuntimeException("Student role not found"));
        List<User> allStudents = userRepository.findByRolesContaining(studentRole);
        List<User> approvedStudents = allStudents.stream()
                .filter(student -> student.getStatus() == UserStatus.APPROVED)
                .filter(student -> !course.getStudents().contains(student))
                .toList();
        
        model.addAttribute("course", course);
        model.addAttribute("students", approvedStudents);
        return "admin/add_student";
    }

    @PostMapping("/{id}/add-student")
    public String addStudent(@PathVariable Long id,
                            @RequestParam Long studentId,
                            RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            courseService.addStudentToCourse(id, studentId);
            String successMessage = messageSource.getMessage("courses.add.student.success", null, locale);
            redirectAttributes.addFlashAttribute("success", successMessage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/courses/" + id;
    }

    @GetMapping("/{id}")
    public String courseDetailsPage(@PathVariable Long id, Model model) {
        Course course = courseService.findById(id);
        model.addAttribute("course", course);
        return "admin/course_details";
    }

    @GetMapping("/{id}/participants")
    public String courseParticipantsPage(@PathVariable Long id, Model model) {
        Course course = courseService.findById(id);
        List<User> participants = courseService.getCourseParticipants(id);
        
        model.addAttribute("course", course);
        model.addAttribute("participants", participants);
        return "admin/course_participants";
    }

    @PostMapping("/{id}/remove-student")
    public String removeStudent(@PathVariable Long id,
                               @RequestParam Long studentId,
                               RedirectAttributes redirectAttributes) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            courseService.removeStudentFromCourse(id, studentId);
            String successMessage = messageSource.getMessage("courses.remove.student.success", null, locale);
            redirectAttributes.addFlashAttribute("success", successMessage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/courses/" + id;
    }
}

