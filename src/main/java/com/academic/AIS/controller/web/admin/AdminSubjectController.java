package com.academic.AIS.controller.web.admin;

import com.academic.AIS.model.Subject;
import com.academic.AIS.model.Teacher;
import com.academic.AIS.model.StudyGroup;
import com.academic.AIS.model.SubjectAssignment;
import com.academic.AIS.service.SubjectManagementService;
import com.academic.AIS.service.TeacherManagementService;
import com.academic.AIS.service.GroupManagementService;
import com.academic.AIS.service.AssignmentManagementService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/admin/subjects")
@PreAuthorize("hasRole('ADMINISTRATOR')")
@Validated
public class AdminSubjectController extends BaseAdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminSubjectController.class);

    private final SubjectManagementService subjectManagementService;
    private final TeacherManagementService teacherManagementService;
    private final GroupManagementService groupManagementService;
    private final AssignmentManagementService assignmentManagementService;

    @Autowired
    public AdminSubjectController(SubjectManagementService subjectManagementService,
                                  TeacherManagementService teacherManagementService,
                                  GroupManagementService groupManagementService,
                                  AssignmentManagementService assignmentManagementService) {
        this.subjectManagementService = subjectManagementService;
        this.teacherManagementService = teacherManagementService;
        this.groupManagementService = groupManagementService;
        this.assignmentManagementService = assignmentManagementService;
    }

    @GetMapping
    public String listSubjects(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            List<Subject> subjects = subjectManagementService.getAllSubjects();
            List<Teacher> teachers = teacherManagementService.getAllTeachers();
            List<StudyGroup> groups = groupManagementService.getAllGroups();

            logger.info("Loading subjects page - Subjects: {}, Teachers: {}, Groups: {}",
                    subjects.size(), teachers.size(), groups.size());

            model.addAttribute("subjects", subjects);
            model.addAttribute("teachers", teachers);
            model.addAttribute("groups", groups);
            addCurrentUserToModel(session, model);

            return "admin/subjects";
        } catch (Exception e) {
            logger.error("Error loading subjects page", e);
            redirectAttributes.addFlashAttribute("error", "Error loading subjects: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/create")
    public String createSubject(@RequestParam @NotBlank(message = "Subject name is required") String subjectName,
                                @RequestParam @NotBlank(message = "Subject code is required") String subjectCode,
                                @RequestParam @NotNull(message = "Credits are required") @Positive(message = "Credits must be positive") Integer credits,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String academicYear,
                                @RequestParam(required = false) String semester,
                                @RequestParam(required = false) Integer teacherId,
                                @RequestParam(required = false) Integer groupId,
                                RedirectAttributes redirectAttributes) {
        try {
            logger.info("Creating subject: {} ({})", subjectName, subjectCode);

            Subject subject = subjectManagementService.createSubject(
                    subjectName, subjectCode, credits, description);

            if (teacherId != null && teacherId > 0 && groupId != null && groupId > 0) {
                String year = (academicYear != null && !academicYear.trim().isEmpty())
                        ? academicYear : "2024/2025";
                String sem = (semester != null && !semester.trim().isEmpty())
                        ? semester : "Fall";

                logger.info("Creating assignment: Subject={}, Teacher={}, Group={}, Year={}, Semester={}",
                        subject.getSubjectId(), teacherId, groupId, year, sem);

                assignmentManagementService.createAssignment(
                        subject.getSubjectId(), teacherId, groupId, year, sem);
            }

            redirectAttributes.addFlashAttribute("success", "Subject created successfully");
        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating subject", e);
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating subject", e);
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
        }

        return "redirect:/admin/subjects";
    }

    @PostMapping("/update/{id}")
    public String updateSubject(@PathVariable @NotNull Integer id,
                                @RequestParam @NotBlank(message = "Subject name is required") String subjectName,
                                @RequestParam @NotBlank(message = "Subject code is required") String subjectCode,
                                @RequestParam @NotNull(message = "Credits are required") @Positive(message = "Credits must be positive") Integer credits,
                                @RequestParam(required = false) String description,
                                @RequestParam(required = false) String academicYear,
                                @RequestParam(required = false) String semester,
                                @RequestParam(required = false) Integer teacherId,
                                @RequestParam(required = false) Integer groupId,
                                RedirectAttributes redirectAttributes) {
        try {
            logger.info("Updating subject ID: {}", id);

            subjectManagementService.updateSubject(id, subjectName, subjectCode, credits, description);

            List<SubjectAssignment> existingAssignments = assignmentManagementService
                    .getAssignmentsBySubject(id);

            for (SubjectAssignment assignment : existingAssignments) {
                assignmentManagementService.deleteAssignment(assignment.getAssignmentId());
            }

            if (teacherId != null && teacherId > 0 && groupId != null && groupId > 0) {
                String year = (academicYear != null && !academicYear.trim().isEmpty())
                        ? academicYear : "2024/2025";
                String sem = (semester != null && !semester.trim().isEmpty())
                        ? semester : "Fall";

                assignmentManagementService.createAssignment(id, teacherId, groupId, year, sem);
                logger.info("Created new assignment for updated subject");
            }

            redirectAttributes.addFlashAttribute("success", "Subject updated successfully");
        } catch (IllegalArgumentException e) {
            logger.error("Validation error updating subject", e);
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating subject", e);
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
        }

        return "redirect:/admin/subjects";
    }

    @PostMapping("/delete/{id}")
    public String deleteSubject(@PathVariable @NotNull Integer id,
                                RedirectAttributes redirectAttributes) {
        try {
            logger.info("Deleting subject ID: {}", id);

            assignmentManagementService.deleteAssignmentsBySubject(id);

            subjectManagementService.deleteSubject(id);

            redirectAttributes.addFlashAttribute("success", "Subject deleted successfully");
        } catch (IllegalArgumentException e) {
            logger.error("Validation error deleting subject", e);
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error deleting subject", e);
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
        }

        return "redirect:/admin/subjects";
    }
}