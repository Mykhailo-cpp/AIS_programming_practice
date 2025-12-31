package com.academic.AIS.service;

import com.academic.AIS.dto.request.LoginRequest;
import com.academic.AIS.dto.response.AuthResponse;
import com.academic.AIS.exception.DuplicateResourceException;
import com.academic.AIS.exception.UnauthorizedException;
import com.academic.AIS.exception.ValidationException;
import com.academic.AIS.model.*;
import com.academic.AIS.repository.*;
import com.academic.AIS.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private AdministratorRepository administratorRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private Student testStudent;

    @BeforeEach
    void setUp() {
        testUser = new User("john", "hashedPassword", "STUDENT");
        testStudent = new Student(testUser, "John", "Doe", "john@example.com");
    }

    @Test
    void authenticate_ValidCredentials_ReturnsAuthResponse() {
        LoginRequest loginRequest = new LoginRequest("john", "password123");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(studentRepository.findByUsername("john")).thenReturn(Optional.of(testStudent));
        when(jwtTokenProvider.generateToken(anyString(), anyString(), any())).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken("john")).thenReturn("refresh-token");

        AuthResponse response = authenticationService.authenticate(loginRequest);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        verify(userRepository).findByUsername("john");
    }

    @Test
    void authenticate_InvalidUsername_ThrowsUnauthorizedException() {
        LoginRequest loginRequest = new LoginRequest("nonexistent", "password123");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authenticationService.authenticate(loginRequest)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void authenticate_InvalidPassword_ThrowsUnauthorizedException() {
        LoginRequest loginRequest = new LoginRequest("john", "wrongPassword");
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> authenticationService.authenticate(loginRequest)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void authenticate_NullUsername_ThrowsValidationException() {
        LoginRequest loginRequest = new LoginRequest(null, "password123");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationService.authenticate(loginRequest)
        );

        assertEquals("Username and password are required", exception.getMessage());
    }

    @Test
    void authenticate_EmptyPassword_ThrowsValidationException() {
        LoginRequest loginRequest = new LoginRequest("john", "   ");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationService.authenticate(loginRequest)
        );

        assertEquals("Username and password are required", exception.getMessage());
    }

    @Test
    void registerStudent_UsernameExists_ThrowsDuplicateResourceException() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> authenticationService.registerStudent("John", "Doe", "john@example.com")
        );

        assertTrue(exception.getMessage().contains("User"));
        assertTrue(exception.getMessage().contains("username"));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void registerStudent_NullFirstName_ThrowsValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationService.registerStudent(null, "Doe", "john@example.com")
        );

        assertEquals("First name is required", exception.getMessage());
    }

    @Test
    void registerStudent_EmptyEmail_ThrowsValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authenticationService.registerStudent("John", "Doe", "   ")
        );

        assertEquals("Email is required", exception.getMessage());
    }
}