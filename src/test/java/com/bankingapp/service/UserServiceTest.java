package com.bankingapp.service;

import com.bankingapp.entity.User;
import com.bankingapp.exception.UserNotFoundException;
import com.bankingapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("TestPassword123!");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setIsActive(true);
    }

    @Test
    void createUser_Success() {
        // Arrange
        User newUser = new User("newuser", "new@example.com", "NewPassword123!", "Jane", "Smith");
        User savedUser = new User("newuser", "new@example.com", "encodedPassword", "Jane", "Smith");
        savedUser.setId(2L);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(newUser);

        // Assert
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getIsActive());
        verify(auditService).logUserCreation(savedUser);
    }

    @Test
    void createUser_UsernameExists_ThrowsException() {
        // Arrange
        User newUser = new User("existinguser", "new@example.com", "Password123!", "Jane", "Smith");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.createUser(newUser));
        assertEquals("Username is already taken", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_EmailExists_ThrowsException() {
        // Arrange
        User newUser = new User("newuser", "existing@example.com", "Password123!", "Jane", "Smith");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.createUser(newUser));
        assertEquals("Email is already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WeakPassword_ThrowsException() {
        // Arrange
        User newUser = new User("newuser", "new@example.com", "weak", "Jane", "Smith");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.createUser(newUser));
        assertEquals("Password does not meet security requirements", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, 
            () -> userService.getUserById(999L));
        assertEquals("User not found with ID: 999", exception.getMessage());
    }

    @Test
    void getUserByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    void getUserByUsername_NotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, 
            () -> userService.getUserByUsername("nonexistent"));
        assertEquals("User not found with username: nonexistent", exception.getMessage());
    }

    @Test
    void updateUser_Success() {
        // Arrange
        User updateDetails = new User();
        updateDetails.setFirstName("Updated");
        updateDetails.setLastName("Name");
        updateDetails.setPhone("1234567890");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(1L, updateDetails);

        // Assert
        assertNotNull(result);
        assertEquals("Updated", testUser.getFirstName());
        assertEquals("Name", testUser.getLastName());
        assertEquals("1234567890", testUser.getPhone());
        verify(auditService).logUserUpdate(testUser);
    }

    @Test
    void changePassword_Success() {
        // Arrange
        String currentPassword = "OldPassword123!";
        String newPassword = "NewPassword123!";
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        boolean result = userService.changePassword(1L, currentPassword, newPassword);

        // Assert
        assertTrue(result);
        verify(passwordEncoder).encode(newPassword);
        verify(auditService).logPasswordChange(testUser);
    }

    @Test
    void changePassword_WrongCurrentPassword_ThrowsException() {
        // Arrange
        String currentPassword = "WrongPassword";
        String newPassword = "NewPassword123!";
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.changePassword(1L, currentPassword, newPassword));
        assertEquals("Current password is incorrect", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_WeakNewPassword_ThrowsException() {
        // Arrange
        String currentPassword = "OldPassword123!";
        String newPassword = "weak";
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(currentPassword, testUser.getPassword())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.changePassword(1L, currentPassword, newPassword));
        assertEquals("New password does not meet security requirements", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deactivateUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.deactivateUser(1L);

        // Assert
        assertFalse(testUser.getIsActive());
        verify(auditService).logUserDeactivation(testUser);
    }

    @Test
    void activateUser_Success() {
        // Arrange
        testUser.setIsActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        userService.activateUser(1L);

        // Assert
        assertTrue(testUser.getIsActive());
        verify(auditService).logUserActivation(testUser);
    }

    @Test
    void existsByUsername_ReturnsTrue() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act
        boolean result = userService.existsByUsername("testuser");

        // Assert
        assertTrue(result);
    }

    @Test
    void existsByUsername_ReturnsFalse() {
        // Arrange
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // Act
        boolean result = userService.existsByUsername("nonexistent");

        // Assert
        assertFalse(result);
    }

    @Test
    void existsByEmail_ReturnsTrue() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act
        boolean result = userService.existsByEmail("test@example.com");

        // Assert
        assertTrue(result);
    }

    @Test
    void getAllActiveUsers_Success() {
        // Arrange
        User user2 = new User("user2", "user2@example.com", "password", "Jane", "Doe");
        user2.setIsActive(true);
        List<User> activeUsers = Arrays.asList(testUser, user2);
        
        when(userRepository.findByIsActiveTrue()).thenReturn(activeUsers);

        // Act
        List<User> result = userService.getAllActiveUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(User::getIsActive));
    }

    @Test
    void searchUsers_WithSearchTerm_Success() {
        // Arrange
        String searchTerm = "john";
        List<User> searchResults = Arrays.asList(testUser);
        
        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            searchTerm, searchTerm, searchTerm, searchTerm)).thenReturn(searchResults);

        // Act
        List<User> result = userService.searchUsers(searchTerm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getId());
    }

    @Test
    void searchUsers_EmptySearchTerm_ReturnsAllActiveUsers() {
        // Arrange
        List<User> activeUsers = Arrays.asList(testUser);
        when(userRepository.findByIsActiveTrue()).thenReturn(activeUsers);

        // Act
        List<User> result = userService.searchUsers("");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findByIsActiveTrue();
    }

    @Test
    void getUserStatistics_Success() {
        // Arrange
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByIsActiveTrue()).thenReturn(8L);

        // Act
        UserService.UserStatistics result = userService.getUserStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getTotalUsers());
        assertEquals(8L, result.getActiveUsers());
        assertEquals(2L, result.getInactiveUsers());
        assertEquals(80.0, result.getActiveUserPercentage(), 0.01);
    }

    @Test
    void resetPassword_Success() {
        // Arrange
        String email = "test@example.com";
        String newPassword = "NewPassword123!";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        boolean result = userService.resetPassword(email, newPassword);

        // Assert
        assertTrue(result);
        verify(passwordEncoder).encode(newPassword);
        verify(auditService).logPasswordReset(testUser);
    }

    @Test
    void resetPassword_WeakPassword_ThrowsException() {
        // Arrange
        String email = "test@example.com";
        String newPassword = "weak";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.resetPassword(email, newPassword));
        assertEquals("New password does not meet security requirements", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}