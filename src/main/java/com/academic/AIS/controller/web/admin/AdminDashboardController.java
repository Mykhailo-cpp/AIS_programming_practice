package com.academic.AIS.controller.web.admin;

import com.academic.AIS.service.StatisticsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminDashboardController extends BaseAdminController {

    private final StatisticsService statisticsService;

    @Autowired
    public AdminDashboardController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        StatisticsService.SystemStatistics stats = statisticsService.getSystemStatistics();

        model.addAttribute("stats", stats);
        addCurrentUserToModel(session, model);

        return "admin/dashboard";
    }

}