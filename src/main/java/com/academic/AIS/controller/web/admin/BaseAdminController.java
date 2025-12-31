package com.academic.AIS.controller.web.admin;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
public abstract class BaseAdminController {

    protected void addCurrentUserToModel(HttpSession session, Model model) {
        Object displayName = session.getAttribute("displayName");
        if (displayName != null) {
            model.addAttribute("currentUser", displayName);
        }
    }
}