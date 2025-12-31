package com.academic.AIS.controller.web.admin;

import com.academic.AIS.model.StudyGroup;
import com.academic.AIS.service.GroupManagementService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;

@Controller
@RequestMapping("/admin/groups")
@PreAuthorize("hasRole('ADMINISTRATOR')")
@Validated
public class AdminGroupController extends BaseAdminController {

    private final GroupManagementService groupManagementService;

    @Autowired
    public AdminGroupController(GroupManagementService groupManagementService) {
        this.groupManagementService = groupManagementService;
    }

    @GetMapping
    public String listGroups(HttpSession session, Model model) {
        List<StudyGroup> groups = groupManagementService.getAllGroups();
        model.addAttribute("groups", groups);
        addCurrentUserToModel(session, model);

        return "admin/groups";
    }

    @PostMapping("/create")
    public String createGroup(@RequestParam @NotBlank(message = "Group name is required") String groupName,
                              @RequestParam @NotNull(message = "Year is required") Integer year,
                              RedirectAttributes redirectAttributes) {
        try {
            groupManagementService.createGroup(groupName, year);
            redirectAttributes.addFlashAttribute("success", "Group created successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
        }

        return "redirect:/admin/groups";
    }

    @PostMapping("/update/{id}")
    public String updateGroup(@PathVariable @NotNull Integer id,
                              @RequestParam @NotBlank(message = "Group name is required") String groupName,
                              @RequestParam @NotNull(message = "Year is required") Integer year,
                              RedirectAttributes redirectAttributes) {
        try {
            groupManagementService.updateGroup(id, groupName, year);
            redirectAttributes.addFlashAttribute("success", "Group updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
        }

        return "redirect:/admin/groups";
    }

    @PostMapping("/delete/{id}")
    public String deleteGroup(@PathVariable @NotNull Integer id,
                              RedirectAttributes redirectAttributes) {
        try {
            groupManagementService.deleteGroup(id);
            redirectAttributes.addFlashAttribute("success", "Group deleted successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unexpected error occurred");
        }

        return "redirect:/admin/groups";
    }
}