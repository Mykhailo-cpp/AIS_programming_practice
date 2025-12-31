package com.academic.AIS.controller.web.admin;

import com.academic.AIS.model.Teacher;
import com.academic.AIS.service.TeacherManagementService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/teachers")
@PreAuthorize("hasRole('ADMINISTRATOR')")
@Validated
public class AdminTeacherController extends BaseAdminController {

    private final TeacherManagementService teacherManagementService;

    @Autowired
    public AdminTeacherController(TeacherManagementService teacherManagementService) {
        this.teacherManagementService = teacherManagementService;
    }

    @GetMapping
    public String listTeachers(HttpSession session, Model model) {
        List<Teacher> teachers = teacherManagementService.getAllTeachers();
        model.addAttribute("teachers", teachers);
        addCurrentUserToModel(session, model);

        return "admin/teachers";
    }

    @PostMapping("/create")
    public String createTeacher(@RequestParam @NotBlank(message = "First name is required") String firstName,
                                @RequestParam @NotBlank(message = "Last name is required") String lastName,
                                @RequestParam @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
                                RedirectAttributes redirectAttributes) {
        try {
            teacherManagementService.createTeacher(firstName, lastName, email);
            redirectAttributes.addFlashAttribute("success",
                    "Teacher created successfully. Login: " + firstName.toLowerCase() + ", Password: " + lastName);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
        }

        return "redirect:/admin/teachers";
    }

    @PostMapping("/update/{id}")
    public String updateTeacher(@PathVariable @NotNull Integer id,
                                @RequestParam @NotBlank(message = "First name is required") String firstName,
                                @RequestParam @NotBlank(message = "Last name is required") String lastName,
                                @RequestParam @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
                                RedirectAttributes redirectAttributes) {
        try {
            teacherManagementService.updateTeacher(id, firstName, lastName, email);
            redirectAttributes.addFlashAttribute("success", "Teacher updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
        }

        return "redirect:/admin/teachers";
    }

    @PostMapping("/delete/{id}")
    public String deleteTeacher(@PathVariable @NotNull Integer id,
                                RedirectAttributes redirectAttributes) {
        try {
            teacherManagementService.deleteTeacher(id);
            redirectAttributes.addFlashAttribute("success", "Teacher deleted successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
        }

        return "redirect:/admin/teachers";
    }
}