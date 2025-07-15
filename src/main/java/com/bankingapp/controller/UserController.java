package com.bankingapp.controller;

import com.bankingapp.dto.ChangePasswordRequest;
import com.bankingapp.dto.UserDTO;
import com.bankingapp.entity.User;
import com.bankingapp.security.UserPrincipal;
import com.bankingapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        logger.info("Getting current user info for: {}", currentUser.getUsername());
        
        User user = userService.getUserById(currentUser.getId());
        UserDTO userDTO = convertToDTO(user);
        
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(@Valid @RequestBody UserDTO userDTO,
                                                    @AuthenticationPrincipal UserPrincipal currentUser) {
        logger.info("Updating current user: {}", currentUser.getUsername());
        
        User userDetails = convertToEntity(userDTO);
        User updatedUser = userService.updateUser(currentUser.getId(), userDetails);
        UserDTO responseDTO = convertToDTO(updatedUser);
        
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                          @AuthenticationPrincipal UserPrincipal currentUser) {
        logger.info("Password change request for user: {}", currentUser.getUsername());
        
        boolean success = userService.changePassword(
            currentUser.getId(),
            request.getCurrentPassword(),
            request.getNewPassword()
        );
        
        if (success) {
            return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
        } else {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Failed to change password"));
        }
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long userId) {
        logger.info("Admin getting user by ID: {}", userId);
        
        User user = userService.getUserById(userId);
        UserDTO userDTO = convertToDTO(user);
        
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestParam(required = false) String search) {
        logger.info("Admin getting all users with search: {}", search);
        
        List<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userService.searchUsers(search);
        } else {
            users = userService.getAllActiveUsers();
        }
        
        List<UserDTO> userDTOs = users.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(userDTOs);
    }

    @PutMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateUser(@PathVariable Long userId) {
        logger.info("Admin deactivating user: {}", userId);
        
        userService.deactivateUser(userId);
        return ResponseEntity.ok(new ApiResponse(true, "User deactivated successfully"));
    }

    @PutMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateUser(@PathVariable Long userId) {
        logger.info("Admin activating user: {}", userId);
        
        userService.activateUser(userId);
        return ResponseEntity.ok(new ApiResponse(true, "User activated successfully"));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserService.UserStatistics> getUserStatistics() {
        logger.info("Admin getting user statistics");
        
        UserService.UserStatistics stats = userService.getUserStatistics();
        return ResponseEntity.ok(stats);
    }

    // Helper methods
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private User convertToEntity(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        return user;
    }

    // Response DTO classes
    public static class ApiResponse {
        private Boolean success;
        private String message;

        public ApiResponse(Boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

}
