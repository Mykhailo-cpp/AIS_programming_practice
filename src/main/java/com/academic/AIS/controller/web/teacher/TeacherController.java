package com.academic.AIS.controller.web.teacher;

import com.academic.AIS.model.Teacher;
import com.academic.AIS.model.SubjectAssignment;
import com.academic.AIS.model.Grade;
import com.academic.AIS.model.Student;
import com.academic.AIS.service.GradeService;
import com.academic.AIS.service.TeacherManagementService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher")
@PreAuthorize("hasRole('TEACHER')")
@Validated
public class TeacherController {

    private final TeacherManagementService teacherManagementService;
    private final GradeService gradeService;

    @Autowired
    public TeacherController(TeacherManagementService teacherManagementService, GradeService gradeService) {
        this.teacherManagementService = teacherManagementService;
        this.gradeService = gradeService;
    }

    private Integer getCurrentTeacherId(Authentication authentication) {
        String username = authentication.getName();
        return teacherManagementService.getTeacherByUsername(username)
                .map(Teacher::getTeacherId)
                .orElseThrow(() -> new IllegalStateException("Teacher profile not found for user: " + username));
    }

    private void addCurrentUserToModel(Authentication authentication, Model model) {
        String username = authentication.getName();
        teacherManagementService.getTeacherByUsername(username)
                .ifPresent(teacher -> model.addAttribute("currentUser", teacher.getFullName()));
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Integer teacherId = getCurrentTeacherId(authentication);

        List<SubjectAssignment> assignments = teacherManagementService.getTeacherAssignments(teacherId);

        long totalGrades = teacherManagementService.countTeacherGrades(teacherId);

        int totalStudents = assignments.stream()
                .filter(a -> a != null && a.getGroup() != null && a.getGroup().getStudents() != null)
                .flatMap(a -> a.getGroup().getStudents().stream())
                .collect(Collectors.toSet())
                .size();

        List<Grade> teacherGrades = gradeService.getTeacherGrades(teacherId);
        double averageGrade = teacherGrades.isEmpty() ? 0.0 :
                teacherGrades.stream().mapToDouble(Grade::getGradeValue).average().orElse(0.0);

        TeacherStats stats = new TeacherStats(assignments.size(), totalStudents, totalGrades, averageGrade);

        addCurrentUserToModel(authentication, model);
        model.addAttribute("subjects", assignments);
        model.addAttribute("stats", stats);

        return "teacher/dashboard";
    }

    // ==================== GRADES PAGE ====================

    @GetMapping("/grades")
    public String gradesPage(@RequestParam(required = false) Integer subjectId,
                             @RequestParam(required = false) Integer assignmentId,
                             Authentication authentication,
                             Model model) {

        Integer teacherId = getCurrentTeacherId(authentication);

        List<SubjectAssignment> allAssignments = teacherManagementService.getTeacherAssignments(teacherId);
        List<SubjectAssignment> assignments = allAssignments.stream()
                .filter(a -> a != null && a.getSubject() != null)
                .collect(Collectors.toList());

        List<Grade> grades;

        if (assignmentId != null) {
            grades = gradeService.getGradesByAssignment(assignmentId);
        } else if (subjectId != null) {
            grades = gradeService.getGradesForTeacherSubject(teacherId, subjectId);
        } else {
            grades = gradeService.getTeacherGrades(teacherId);
        }

        model.addAttribute("assignments", assignments);
        model.addAttribute("grades", grades);
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("selectedAssignmentId", assignmentId);

        // Build safe JSON for UI
        List<Map<String, Object>> safeAssignments = buildSafeAssignmentsForUI(assignments);
        model.addAttribute("safeAssignments", safeAssignments);

        addCurrentUserToModel(authentication, model);

        return "teacher/grades";
    }

    private List<Map<String, Object>> buildSafeAssignmentsForUI(List<SubjectAssignment> assignments) {
        List<Map<String, Object>> safeAssignments = new ArrayList<>();

        for (SubjectAssignment a : assignments) {
            if (a == null || a.getSubject() == null || a.getGroup() == null) continue;

            Map<String, Object> safe = new HashMap<>();
            safe.put("assignmentId", a.getAssignmentId());

            Map<String, Object> sub = new HashMap<>();
            sub.put("subjectId", a.getSubject().getSubjectId());
            sub.put("subjectName", a.getSubject().getSubjectName());
            safe.put("subject", sub);

            Map<String, Object> grp = new HashMap<>();
            grp.put("groupName", a.getGroup().getGroupName());

            List<Map<String, Object>> studentList = new ArrayList<>();
            if (a.getGroup().getStudents() != null) {
                for (Student s : a.getGroup().getStudents()) {
                    Map<String, Object> st = new HashMap<>();
                    st.put("studentId", s.getStudentId());
                    st.put("firstName", s.getFirstName());
                    st.put("lastName", s.getLastName());
                    studentList.add(st);
                }
            }

            grp.put("students", studentList);
            safe.put("group", grp);

            safeAssignments.add(safe);
        }

        return safeAssignments;
    }

    // ==================== CREATE GRADE ====================

    @PostMapping("/grades/create")
    public String createGrade(@RequestParam @NotNull(message = "Assignment ID is required") Integer assignmentId,
                              @RequestParam @NotNull(message = "Student ID is required") Integer studentId,
                              @RequestParam @NotNull(message = "Grade value is required")
                              @Min(value = 0, message = "Grade must be at least 0")
                              @Max(value = 10, message = "Grade must not exceed 10") Integer gradeValue,
                              @RequestParam(required = false) String comments,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {

        try {
            Integer teacherId = getCurrentTeacherId(authentication);
            gradeService.enterGrade(teacherId, studentId, assignmentId, gradeValue, comments);
            redirectAttributes.addFlashAttribute("success", "Grade added successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
        }

        return "redirect:/teacher/grades?assignmentId=" + assignmentId;
    }

    // ==================== UPDATE GRADE ====================

    @PostMapping("/grades/update/{id}")
    public String updateGrade(@PathVariable @NotNull Integer id,
                              @RequestParam @NotNull(message = "Grade value is required")
                              @Min(value = 0, message = "Grade must be at least 0")
                              @Max(value = 10, message = "Grade must not exceed 10") Integer gradeValue,
                              @RequestParam(required = false) String comments,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {

        Integer teacherId = getCurrentTeacherId(authentication);

        try {
            Grade g = gradeService.updateGrade(id, teacherId, gradeValue, comments);
            redirectAttributes.addFlashAttribute("success", "Grade updated successfully");
            return "redirect:/teacher/grades?assignmentId=" + g.getAssignment().getAssignmentId();

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/teacher/grades";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
            return "redirect:/teacher/grades";
        }
    }

    // ==================== DELETE GRADE ====================

    @PostMapping("/grades/delete/{id}")
    public String deleteGrade(@PathVariable @NotNull Integer id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {

        Integer teacherId = getCurrentTeacherId(authentication);

        try {
            Grade deleted = gradeService.deleteGrade(id, teacherId);
            redirectAttributes.addFlashAttribute("success", "Grade deleted successfully");
            return "redirect:/teacher/grades?assignmentId=" +
                    deleted.getAssignment().getAssignmentId();

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/teacher/grades";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
            return "redirect:/teacher/grades";
        }
    }

    // ==================== STATS CLASS ====================

    public static class TeacherStats {
        private final int totalSubjects;
        private final int totalStudents;
        private final long totalGrades;
        private final double averageGrade;

        public TeacherStats(int totalSubjects, int totalStudents, long totalGrades, double averageGrade) {
            this.totalSubjects = totalSubjects;
            this.totalStudents = totalStudents;
            this.totalGrades = totalGrades;
            this.averageGrade = averageGrade;
        }

        public int getTotalSubjects() { return totalSubjects; }
        public int getTotalStudents() { return totalStudents; }
        public long getTotalGrades() { return totalGrades; }
        public double getAverageGrade() { return averageGrade; }
    }
}