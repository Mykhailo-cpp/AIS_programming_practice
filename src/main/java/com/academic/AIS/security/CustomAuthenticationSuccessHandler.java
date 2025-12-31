package com.academic.AIS.security;

import com.academic.AIS.model.Administrator;
import com.academic.AIS.model.Student;
import com.academic.AIS.model.Teacher;
import com.academic.AIS.repository.AdministratorRepository;
import com.academic.AIS.repository.StudentRepository;
import com.academic.AIS.repository.TeacherRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        System.out.println("=== AUTHENTICATION SUCCESS ===");

        String username = authentication.getName();
        System.out.println("Username: " + username);
        System.out.println("Authorities: " + authentication.getAuthorities());

        String role = authentication.getAuthorities().iterator().next()
                .getAuthority()
                .replace("ROLE_", "");

        System.out.println("Role (cleaned): " + role);

        String displayName = getDisplayName(username, role);
        System.out.println("Display Name: " + displayName);

        request.getSession().setAttribute("username", username);
        request.getSession().setAttribute("role", role);
        request.getSession().setAttribute("displayName", displayName);

        System.out.println("Session attributes set successfully");

        String redirectUrl = switch (role) {
            case "ADMINISTRATOR" -> "/admin/dashboard";
            case "TEACHER" -> "/teacher/dashboard";
            case "STUDENT" -> "/student/dashboard";
            default -> "/login";
        };

        System.out.println("Redirecting to: " + redirectUrl);
        System.out.println("=== END AUTHENTICATION SUCCESS ===");

        response.sendRedirect(redirectUrl);
    }

    private String getDisplayName(String username, String role) {
        System.out.println("  -> Fetching display name for: " + username + " with role: " + role);

        String displayName = switch (role) {
            case "STUDENT" -> {
                System.out.println("  -> Querying StudentRepository...");
                yield studentRepository.findByUsername(username)
                        .map(Student::getFullName)
                        .orElse(username);
            }

            case "TEACHER" -> {
                System.out.println("  -> Querying TeacherRepository...");
                yield teacherRepository.findByUsername(username)
                        .map(Teacher::getFullName)
                        .orElse(username);
            }

            case "ADMINISTRATOR" -> {
                System.out.println("  -> Querying AdministratorRepository...");
                yield administratorRepository.findByUsername(username)
                        .map(Administrator::getFullName)
                        .orElse(username);
            }

            default -> {
                System.out.println("  -> Unknown role, using username");
                yield username;
            }
        };

        System.out.println("  -> Display name result: " + displayName);
        return displayName;
    }
}