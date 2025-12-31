package com.academic.AIS.service;

import com.academic.AIS.dto.request.LoginRequest;
import com.academic.AIS.dto.response.AuthResponse;
import com.academic.AIS.exception.DuplicateResourceException;
import com.academic.AIS.exception.UnauthorizedException;
import com.academic.AIS.exception.ValidationException;
import com.academic.AIS.model.User;
import com.academic.AIS.model.Student;
import com.academic.AIS.model.Teacher;
import com.academic.AIS.model.Administrator;
import com.academic.AIS.repository.StudentRepository;
import com.academic.AIS.repository.UserRepository;
import com.academic.AIS.repository.TeacherRepository;
import com.academic.AIS.repository.AdministratorRepository;
import com.academic.AIS.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final AdministratorRepository administratorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthenticationService(UserRepository userRepository,
                                 StudentRepository studentRepository,
                                 TeacherRepository teacherRepository,
                                 AdministratorRepository administratorRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.administratorRepository = administratorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse authenticate(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            throw new ValidationException("Username and password are required");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String displayName = getDisplayName(user);
        String accessToken = jwtTokenProvider.generateToken(
                user.getUsername(),
                user.getRole(),
                user.getUserId()
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getUsername(),
                user.getRole(),
                user.getUserId(),
                displayName
        );
    }

    public Student registerStudent(String firstName, String lastName, String email) {
        validateInput(firstName, lastName, email);

        String username = firstName.toLowerCase();
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("User", "username", username);
        }

        String hashedPassword = passwordEncoder.encode(lastName);

        User user = new User(username, hashedPassword, "STUDENT");
        user = userRepository.save(user);

        Student student = new Student(user, firstName, lastName, email);
        return studentRepository.save(student);
    }

    public Teacher registerTeacher(String firstName, String lastName, String email) {
        validateInput(firstName, lastName, email);

        String username = firstName.toLowerCase();
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("User", "username", username);
        }

        String hashedPassword = passwordEncoder.encode(lastName);

        User user = new User(username, hashedPassword, "TEACHER");
        user = userRepository.save(user);

        Teacher teacher = new Teacher(user, firstName, lastName, email);
        return teacherRepository.save(teacher);
    }

    private String getDisplayName(User user) {
        switch (user.getRole()) {
            case "STUDENT":
                return studentRepository.findByUsername(user.getUsername())
                        .map(Student::getFullName)
                        .orElse(user.getUsername());

            case "TEACHER":
                return teacherRepository.findByUsername(user.getUsername())
                        .map(Teacher::getFullName)
                        .orElse(user.getUsername());

            case "ADMINISTRATOR":
                return administratorRepository.findByUsername(user.getUsername())
                        .map(Administrator::getFullName)
                        .orElse(user.getUsername());

            default:
                return user.getUsername();
        }
    }

    private void validateInput(String firstName, String lastName, String email) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new ValidationException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new ValidationException("Last name is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
    }
}