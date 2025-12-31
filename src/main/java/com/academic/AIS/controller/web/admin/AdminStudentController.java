package com.academic.AIS.controller.web.admin;

import com.academic.AIS.dto.request.CreateStudentRequest;
import com.academic.AIS.dto.response.StudentResponse;
import com.academic.AIS.model.StudyGroup;
import com.academic.AIS.service.StudentManagementService;
import com.academic.AIS.service.GroupManagementService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/admin/students")
@PreAuthorize("hasRole('ADMINISTRATOR')")
@Validated
public class AdminStudentController extends BaseAdminController {

    private final StudentManagementService studentManagementService;
    private final GroupManagementService groupManagementService;

    @Autowired
    public AdminStudentController(StudentManagementService studentManagementService,
                                  GroupManagementService groupManagementService) {
        this.studentManagementService = studentManagementService;
        this.groupManagementService = groupManagementService;
    }

    @GetMapping
    public String listStudents(HttpSession session, Model model) {
        List<StudentResponse> students = studentManagementService.getAllStudents();
        List<StudyGroup> groups = groupManagementService.getAllGroups();

        model.addAttribute("students", students);
        model.addAttribute("groups", groups);
        addCurrentUserToModel(session, model);

        return "admin/students";
    }

    @PostMapping("/create")
    public String createStudent(@RequestParam @NotBlank(message = "First name is required") String firstName,
                                @RequestParam @NotBlank(message = "Last name is required") String lastName,
                                @RequestParam @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
                                @RequestParam(required = false) Integer groupId,
                                RedirectAttributes redirectAttributes) {
        try {
            CreateStudentRequest request = new CreateStudentRequest();
            request.setFirstName(firstName);
            request.setLastName(lastName);
            request.setEmail(email);
            request.setGroupId(groupId);

            studentManagementService.createStudent(request);

            redirectAttributes.addFlashAttribute("success",
                    "Student created successfully. Login: " + firstName.toLowerCase() + ", Password: " + lastName);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
        }

        return "redirect:/admin/students";
    }

    @PostMapping("/update/{id}")
    public String updateStudent(@PathVariable @NotNull Integer id,
                                @RequestParam @NotBlank(message = "First name is required") String firstName,
                                @RequestParam @NotBlank(message = "Last name is required") String lastName,
                                @RequestParam @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
                                @RequestParam(required = false) Integer groupId,
                                RedirectAttributes redirectAttributes) {
        try {
            CreateStudentRequest request = new CreateStudentRequest();
            request.setFirstName(firstName);
            request.setLastName(lastName);
            request.setEmail(email);
            request.setGroupId(groupId);

            studentManagementService.updateStudent(id, request);

            redirectAttributes.addFlashAttribute("success", "Student updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
        }

        return "redirect:/admin/students";
    }

    @PostMapping("/delete/{id}")
    public String deleteStudent(@PathVariable @NotNull Integer id,
                                RedirectAttributes redirectAttributes) {
        try {
            studentManagementService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("success", "Student deleted successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
        }

        return "redirect:/admin/students";
    }

    @PostMapping("/{studentId}/assign-group/{groupId}")
    public String assignStudentToGroup(@PathVariable @NotNull Integer studentId,
                                       @PathVariable @NotNull Integer groupId,
                                       RedirectAttributes redirectAttributes) {
        try {
            studentManagementService.assignStudentToGroup(studentId, groupId);
            redirectAttributes.addFlashAttribute("success", "Student assigned to group successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
        }

        return "redirect:/admin/students";
    }

    @PostMapping("/{studentId}/remove-group")
    public String removeStudentFromGroup(@PathVariable @NotNull Integer studentId,
                                         RedirectAttributes redirectAttributes) {
        try {
            studentManagementService.removeStudentFromGroup(studentId);
            redirectAttributes.addFlashAttribute("success", "Student removed from group successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
        }

        return "redirect:/admin/students";
    }
}