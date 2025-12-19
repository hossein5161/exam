package ir.maktabsharif.onlineexam.controller;
import ir.maktabsharif.onlineexam.model.entity.Course;
import ir.maktabsharif.onlineexam.model.entity.Role;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.model.enums.UserStatus;
import ir.maktabsharif.onlineexam.repository.RoleRepository;
import ir.maktabsharif.onlineexam.repository.UserRepository;
import ir.maktabsharif.onlineexam.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/courses")
public class CourseController {

    private final CourseService courseService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

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
        try {
            courseService.createCourse(course);
            redirectAttributes.addFlashAttribute("success", "دوره با موفقیت ایجاد شد");
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
        try {
            courseService.updateCourse(id, course);
            redirectAttributes.addFlashAttribute("success", "دوره با موفقیت به‌روزرسانی شد");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/courses/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteCourse(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            courseService.deleteCourse(id);
            redirectAttributes.addFlashAttribute("success", "دوره با موفقیت حذف شد");
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
        try {
            courseService.assignTeacherToCourse(id, teacherId);
            redirectAttributes.addFlashAttribute("success", "استاد با موفقیت به دوره اضافه شد");
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
        try {
            courseService.addStudentToCourse(id, studentId);
            redirectAttributes.addFlashAttribute("success", "دانشجو با موفقیت به دوره اضافه شد");
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
        try {
            courseService.removeStudentFromCourse(id, studentId);
            redirectAttributes.addFlashAttribute("success", "دانشجو با موفقیت از دوره حذف شد");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/courses/" + id;
    }
}

