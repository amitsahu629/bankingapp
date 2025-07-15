package com.bankingapp.service;

import com.bankingapp.entity.User;
import com.bankingapp.exception.UserNotFoundException;
import com.bankingapp.repository.UserRepository;
import com.bankingapp.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing User entities and authentication
 * Implements UserDetailsService for Spring Security integration
 */
@Service
@Transactional
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditService auditService;

    /**
     * Create a new user account
     * @param user User entity to create
     * @return Created user with encoded password
     * @throws RuntimeException if user already exists
     */
    public User createUser(@Valid User user) {
        logger.info("Creating new user: {}", user.getUsername());

        // Check if username already exists
        if (existsByUsername(user.getUsername())) {
            logger.error("Username already exists: {}", user.getUsername());
            throw new RuntimeException("Username is already taken");
        }

        // Check if email already exists
        if (existsByEmail(user.getEmail())) {
            logger.error("Email already exists: {}", user.getEmail());
            throw new RuntimeException("Email is already in use");
        }

        // Validate password strength
        if (!isValidPassword(user.getPassword())) {
            logger.error("Password does not meet requirements for user: {}", user.getUsername());
            throw new RuntimeException("Password does not meet security requirements");
        }

        // Encode password
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // Set default values
        user.setIsActive(true);

        try {
            User savedUser = userRepository.save(user);
            logger.info("User created successfully: {} with ID: {}", savedUser.getUsername(), savedUser.getId());

            // Log audit event
            auditService.logUserCreation(savedUser);

            return savedUser;
        } catch (Exception e) {
            logger.error("Failed to create user: {}", user.getUsername(), e);
            throw new RuntimeException("Failed to create user account");
        }
    }

    /**
     * Get user by ID
     * @param userId User ID
     * @return User entity
     * @throws UserNotFoundException if user not found
     */
    public User getUserById(Long userId) {
        logger.debug("Fetching user by ID: {}", userId);
        
        return userRepository.findById(userId)
            .orElseThrow(() -> {
                logger.error("User not found with ID: {}", userId);
                return new UserNotFoundException("User not found with ID: " + userId);
            });
    }

    /**
     * Get user by username
     * @param username Username
     * @return User entity
     * @throws UserNotFoundException if user not found
     */
    public User getUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);
        
        return userRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.error("User not found with username: {}", username);
                return new UserNotFoundException("User not found with username: " + username);
            });
    }

    /**
     * Get user by email
     * @param email Email address
     * @return User entity
     * @throws UserNotFoundException if user not found
     */
    public User getUserByEmail(String email) {
        logger.debug("Fetching user by email: {}", email);
        
        return userRepository.findByEmail(email)
            .orElseThrow(() -> {
                logger.error("User not found with email: {}", email);
                return new UserNotFoundException("User not found with email: " + email);
            });
    }

    /**
     * Update user profile information
     * @param userId User ID
     * @param userDetails Updated user details
     * @return Updated user entity
     */
    public User updateUser(Long userId, User userDetails) {
        logger.info("Updating user: {}", userId);

        User existingUser = getUserById(userId);

        // Update allowed fields
        if (userDetails.getFirstName() != null && !userDetails.getFirstName().trim().isEmpty()) {
            existingUser.setFirstName(userDetails.getFirstName().trim());
        }

        if (userDetails.getLastName() != null && !userDetails.getLastName().trim().isEmpty()) {
            existingUser.setLastName(userDetails.getLastName().trim());
        }

        if (userDetails.getPhone() != null) {
            existingUser.setPhone(userDetails.getPhone().trim());
        }

        if (userDetails.getEmail() != null && !userDetails.getEmail().equals(existingUser.getEmail())) {
            // Check if new email is already in use
            if (existsByEmail(userDetails.getEmail())) {
                logger.error("Email already exists: {}", userDetails.getEmail());
                throw new RuntimeException("Email is already in use");
            }
            existingUser.setEmail(userDetails.getEmail());
        }

        try {
            User updatedUser = userRepository.save(existingUser);
            logger.info("User updated successfully: {}", updatedUser.getUsername());

            // Log audit event
            auditService.logUserUpdate(updatedUser);

            return updatedUser;
        } catch (Exception e) {
            logger.error("Failed to update user: {}", userId, e);
            throw new RuntimeException("Failed to update user profile");
        }
    }

    /**
     * Change user password
     * @param userId User ID
     * @param currentPassword Current password
     * @param newPassword New password
     * @return Success status
     */
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Changing password for user: {}", userId);

        User user = getUserById(userId);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            logger.error("Current password verification failed for user: {}", userId);
            throw new RuntimeException("Current password is incorrect");
        }

        // Validate new password
        if (!isValidPassword(newPassword)) {
            logger.error("New password does not meet requirements for user: {}", userId);
            throw new RuntimeException("New password does not meet security requirements");
        }

        // Encode and save new password
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);

        try {
            userRepository.save(user);
            logger.info("Password changed successfully for user: {}", userId);

            // Log audit event
            auditService.logPasswordChange(user);

            return true;
        } catch (Exception e) {
            logger.error("Failed to change password for user: {}", userId, e);
            throw new RuntimeException("Failed to change password");
        }
    }

    /**
     * Deactivate user account
     * @param userId User ID
     */
    public void deactivateUser(Long userId) {
        logger.info("Deactivating user: {}", userId);

        User user = getUserById(userId);
        user.setIsActive(false);

        try {
            userRepository.save(user);
            logger.info("User deactivated successfully: {}", user.getUsername());

            // Log audit event
            auditService.logUserDeactivation(user);

        } catch (Exception e) {
            logger.error("Failed to deactivate user: {}", userId, e);
            throw new RuntimeException("Failed to deactivate user account");
        }
    }

    /**
     * Activate user account
     * @param userId User ID
     */
    public void activateUser(Long userId) {
        logger.info("Activating user: {}", userId);

        User user = getUserById(userId);
        user.setIsActive(true);

        try {
            userRepository.save(user);
            logger.info("User activated successfully: {}", user.getUsername());

            // Log audit event
            auditService.logUserActivation(user);

        } catch (Exception e) {
            logger.error("Failed to activate user: {}", userId, e);
            throw new RuntimeException("Failed to activate user account");
        }
    }

    /**
     * Get all active users (Admin functionality)
     * @return List of active users
     */
    public List<User> getAllActiveUsers() {
        logger.debug("Fetching all active users");
        return userRepository.findByIsActiveTrue();
    }

    /**
     * Get all users (Admin functionality)
     * @return List of all users
     */
    public List<User> getAllUsers() {
        logger.debug("Fetching all users");
        return userRepository.findAll();
    }

    /**
     * Check if username exists
     * @param username Username to check
     * @return True if exists, false otherwise
     */
    public boolean existsByUsername(String username) {
        boolean exists = userRepository.existsByUsername(username);
        logger.debug("Username '{}' exists: {}", username, exists);
        return exists;
    }

    /**
     * Check if email exists
     * @param email Email to check
     * @return True if exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        boolean exists = userRepository.existsByEmail(email);
        logger.debug("Email '{}' exists: {}", email, exists);
        return exists;
    }

    /**
     * Search users by criteria (Admin functionality)
     * @param searchTerm Search term
     * @return List of matching users
     */
    public List<User> searchUsers(String searchTerm) {
        logger.debug("Searching users with term: {}", searchTerm);
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllActiveUsers();
        }
        
        String searchPattern = "%" + searchTerm.toLowerCase() + "%";
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            searchTerm, searchTerm, searchTerm, searchTerm);
    }

    /**
     * Get user statistics (Admin functionality)
     * @return User statistics
     */
    public UserStatistics getUserStatistics() {
        logger.debug("Calculating user statistics");
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActiveTrue();
        long inactiveUsers = totalUsers - activeUsers;
        
        return new UserStatistics(totalUsers, activeUsers, inactiveUsers);
    }

    /**
     * Validate password strength
     * @param password Password to validate
     * @return True if valid, false otherwise
     */
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        // Check for at least one uppercase letter
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        
        // Check for at least one lowercase letter
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        
        // Check for at least one digit
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        
        // Check for at least one special character
        boolean hasSpecialChar = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }

    /**
     * Implementation of UserDetailsService for Spring Security
     * @param username Username
     * @return UserDetails for authentication
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Loading user by username for authentication: {}", username);

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> {
                logger.error("User not found for authentication: {}", username);
                return new UsernameNotFoundException("User not found with username: " + username);
            });

        if (!user.getIsActive()) {
            logger.error("User account is deactivated: {}", username);
            throw new UsernameNotFoundException("User account is deactivated");
        }

        logger.debug("User loaded successfully for authentication: {}", username);
        return UserPrincipal.create(user);
    }

    /**
     * Load user by ID for JWT authentication
     * @param id User ID
     * @return UserDetails for authentication
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        logger.debug("Loading user by ID for JWT authentication: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> {
                logger.error("User not found for JWT authentication: {}", id);
                return new UsernameNotFoundException("User not found with id: " + id);
            });

        if (!user.getIsActive()) {
            logger.error("User account is deactivated for JWT authentication: {}", id);
            throw new UsernameNotFoundException("User account is deactivated");
        }

        logger.debug("User loaded successfully for JWT authentication: {}", id);
        return UserPrincipal.create(user);
    }

    /**
     * Reset password (for forgot password functionality)
     * @param email User email
     * @param newPassword New password
     * @return Success status
     */
    public boolean resetPassword(String email, String newPassword) {
        logger.info("Resetting password for email: {}", email);

        User user = getUserByEmail(email);

        // Validate new password
        if (!isValidPassword(newPassword)) {
            logger.error("New password does not meet requirements for email: {}", email);
            throw new RuntimeException("New password does not meet security requirements");
        }

        // Encode and save new password
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);

        try {
            userRepository.save(user);
            logger.info("Password reset successfully for email: {}", email);

            // Log audit event
            auditService.logPasswordReset(user);

            return true;
        } catch (Exception e) {
            logger.error("Failed to reset password for email: {}", email, e);
            throw new RuntimeException("Failed to reset password");
        }
    }

    /**
     * Inner class for user statistics
     */
    public static class UserStatistics {
        private final long totalUsers;
        private final long activeUsers;
        private final long inactiveUsers;

        public UserStatistics(long totalUsers, long activeUsers, long inactiveUsers) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.inactiveUsers = inactiveUsers;
        }

        public long getTotalUsers() { return totalUsers; }
        public long getActiveUsers() { return activeUsers; }
        public long getInactiveUsers() { return inactiveUsers; }
        
        public double getActiveUserPercentage() {
            return totalUsers > 0 ? (double) activeUsers / totalUsers * 100 : 0;
        }
    }
}
