package com.academic.AIS.controller.web.student;

import com.academic.AIS.model.Grade;
import com.academic.AIS.model.Student;
import com.academic.AIS.model.Subject;
import com.academic.AIS.repository.GradeRepository;
import com.academic.AIS.repository.StudentRepository;
import com.academic.AIS.repository.SubjectRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final SubjectRepository subjectRepository;

    @Autowired
    public StudentController(StudentRepository studentRepository,
                             GradeRepository gradeRepository,
                             SubjectRepository subjectRepository) {
        this.studentRepository = studentRepository;
        this.gradeRepository = gradeRepository;
        this.subjectRepository = subjectRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication,
                            HttpSession session,
                            Model model,
                            @RequestParam(required = false) Integer subjectId) {

        String username = authentication.getName();

        Student student = studentRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<Grade> grades;
        if (subjectId != null) {
            grades = gradeRepository.findByStudent_StudentIdAndAssignment_Subject_SubjectId(
                    student.getStudentId(), subjectId);
        } else {
            grades = gradeRepository.findByStudent_StudentId(student.getStudentId());
        }

        List<Subject> subjects = subjectRepository.findAll();

        StudentStatistics stats = calculateStatistics(grades);

        model.addAttribute("student", student);
        model.addAttribute("grades", grades);
        model.addAttribute("subjects", subjects);
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("stats", stats);
        model.addAttribute("currentUser", student.getFullName());

        return "student/dashboard";
    }

    private StudentStatistics calculateStatistics(List<Grade> grades) {
        StudentStatistics stats = new StudentStatistics();

        stats.totalGrades = grades.size();

        if (!grades.isEmpty()) {
            double sum = grades.stream()
                    .mapToDouble(Grade::getGradeValue)
                    .sum();
            stats.averageGrade = sum / grades.size();

            stats.passingGrades = (int) grades.stream()
                    .filter(g -> g.getGradeValue() >= 5.0)
                    .count();

            stats.failingGrades = stats.totalGrades - stats.passingGrades;
        }

        return stats;
    }

    public static class StudentStatistics {
        private int totalGrades = 0;
        private Double averageGrade = 0.0;
        private int passingGrades = 0;
        private int failingGrades = 0;

        public int getTotalGrades() { return totalGrades; }
        public Double getAverageGrade() { return averageGrade; }
        public int getPassingGrades() { return passingGrades; }
        public int getFailingGrades() { return failingGrades; }
    }
}