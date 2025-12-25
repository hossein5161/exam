package ir.maktabsharif.onlineexam.controller;
import ir.maktabsharif.onlineexam.model.entity.Course;
import ir.maktabsharif.onlineexam.model.entity.Exam;
import ir.maktabsharif.onlineexam.model.entity.User;
import ir.maktabsharif.onlineexam.repository.CourseRepository;
import ir.maktabsharif.onlineexam.service.CourseService;
import ir.maktabsharif.onlineexam.service.ExamService;
import ir.maktabsharif.onlineexam.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("/teacher")
public class TeacherController {

    private final CourseRepository courseRepository;
    private final CourseService courseService;
    private final ExamService examService;
    private final UserService userService;
    private final MessageSource messageSource;

    private User getCurrentTeacher() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.findByUsername(username);
    }

    @GetMapping("/courses")
    public String teacherCoursesPage(Model model) {
        User teacher = getCurrentTeacher();
        List<Course> courses = courseRepository.findByTeacherWithStudents(teacher);
        model.addAttribute("courses", courses);
        return "teacher/courses";
    }

    @GetMapping("/courses/{courseId}/exams")
    public String courseExamsPage(@PathVariable Long courseId, Model model) {
        User teacher = getCurrentTeacher();
        Course course = courseService.findById(courseId);
        
        if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
            String errorMsg = messageSource.getMessage("error.unauthorized", null, LocaleContextHolder.getLocale());
            return "redirect:/teacher/courses?error=" + errorMsg;
        }
        
        List<Exam> exams = examService.getExamsByCourse(course);
        model.addAttribute("course", course);
        model.addAttribute("exams", exams);
        model.addAttribute("currentTeacher", teacher);
        return "teacher/exams";
    }

    @GetMapping("/courses/{courseId}/exams/new")
    public String newExamPage(@PathVariable Long courseId, Model model) {
        User teacher = getCurrentTeacher();
        Course course = courseService.findById(courseId);
        
        if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
            String errorMsg = messageSource.getMessage("error.unauthorized", null, LocaleContextHolder.getLocale());
            return "redirect:/teacher/courses?error=" + errorMsg;
        }
        
        Exam exam = new Exam();
        exam.setCourse(course);
        exam.setTeacher(teacher);
        
        model.addAttribute("exam", exam);
        model.addAttribute("course", course);
        return "teacher/new_exam";
    }

    @PostMapping("/courses/{courseId}/exams")
    public String createExam(@PathVariable Long courseId,
                            @ModelAttribute Exam exam,
                            RedirectAttributes redirectAttributes) {
        try {
            User teacher = getCurrentTeacher();
            Course course = courseService.findById(courseId);
            
            if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
                String errorMsg = messageSource.getMessage("error.unauthorized", null, LocaleContextHolder.getLocale());
                redirectAttributes.addFlashAttribute("error", errorMsg);
                return "redirect:/teacher/courses";
            }
            
            exam.setCourse(course);
            exam.setTeacher(teacher);
            
            examService.createExam(exam);
            String successMsg = messageSource.getMessage("exams.create.success", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("success", successMsg);
        } catch (Exception e) {
            String errorMsg = messageSource.getMessage(e.getMessage(), null, e.getMessage(), LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("error", errorMsg);
        }
        return "redirect:/teacher/courses/" + courseId + "/exams";
    }

    @GetMapping("/courses/{courseId}/exams/{examId}/edit")
    public String editExamPage(@PathVariable Long courseId,
                               @PathVariable Long examId,
                               Model model) {
        User teacher = getCurrentTeacher();
        Course course = courseService.findById(courseId);
        Exam exam = examService.findById(examId);
        
        if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
            Locale locale = LocaleContextHolder.getLocale();
            String errorMsg = messageSource.getMessage("error.unauthorized", null, locale);
            return "redirect:/teacher/courses?error=" + errorMsg;
        }
        if (!exam.getTeacher().getId().equals(teacher.getId())) {
            String errorMsg = messageSource.getMessage("error.unauthorized", null, LocaleContextHolder.getLocale());
            return "redirect:/teacher/courses/" + courseId + "/exams?error=" + errorMsg;
        }
        
        model.addAttribute("exam", exam);
        model.addAttribute("course", course);
        return "teacher/edit_exam";
    }

    @PostMapping("/courses/{courseId}/exams/{examId}/edit")
    public String updateExam(@PathVariable Long courseId,
                            @PathVariable Long examId,
                            @ModelAttribute Exam exam,
                            RedirectAttributes redirectAttributes) {
        try {
            User teacher = getCurrentTeacher();
            Course course = courseService.findById(courseId);
            
            if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
                Locale locale = LocaleContextHolder.getLocale();
                String errorMsg = messageSource.getMessage("error.unauthorized", null, locale);
                redirectAttributes.addFlashAttribute("error", errorMsg);
                return "redirect:/teacher/courses";
            }
            if (!examService.isExamOwner(examId, teacher.getId())) {
                String errorMsg = messageSource.getMessage("error.unauthorized", null, LocaleContextHolder.getLocale());
                redirectAttributes.addFlashAttribute("error", errorMsg);
                return "redirect:/teacher/courses/" + courseId + "/exams";
            }
            
            examService.updateExam(examId, exam);
            String successMsg = messageSource.getMessage("exams.update.success", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("success", successMsg);
        } catch (Exception e) {
            String errorMsg = messageSource.getMessage(e.getMessage(), null, e.getMessage(), LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("error", errorMsg);
        }
        return "redirect:/teacher/courses/" + courseId + "/exams";
    }

    @PostMapping("/courses/{courseId}/exams/{examId}/delete")
    public String deleteExam(@PathVariable Long courseId,
                            @PathVariable Long examId,
                            RedirectAttributes redirectAttributes) {
        try {
            User teacher = getCurrentTeacher();
            Course course = courseService.findById(courseId);
            
            if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
                Locale locale = LocaleContextHolder.getLocale();
                String errorMsg = messageSource.getMessage("error.unauthorized", null, locale);
                redirectAttributes.addFlashAttribute("error", errorMsg);
                return "redirect:/teacher/courses";
            }
            if (!examService.isExamOwner(examId, teacher.getId())) {
                String errorMsg = messageSource.getMessage("error.unauthorized", null, LocaleContextHolder.getLocale());
                redirectAttributes.addFlashAttribute("error", errorMsg);
                return "redirect:/teacher/courses/" + courseId + "/exams";
            }
            
            examService.deleteExam(examId);
            String successMsg = messageSource.getMessage("exams.delete.success", null, LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("success", successMsg);
        } catch (Exception e) {
            String errorMsg = messageSource.getMessage(e.getMessage(), null, e.getMessage(), LocaleContextHolder.getLocale());
            redirectAttributes.addFlashAttribute("error", errorMsg);
        }
        return "redirect:/teacher/courses/" + courseId + "/exams";
    }
}

