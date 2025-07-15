package com.bankingapp.service;

import com.bankingapp.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class AuditService {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    
    @Autowired(required = false)
    private HttpServletRequest request;
    
    public void logUserCreation(User user) {
        String ipAddress = getClientIpAddress();
        auditLogger.info("USER_CREATED - User: {} (ID: {}), IP: {}", 
                        user.getUsername(), user.getId(), ipAddress);
    }
    
    public void logUserUpdate(User user) {
        String ipAddress = getClientIpAddress();
        auditLogger.info("USER_UPDATED - User: {} (ID: {}), IP: {}", 
                        user.getUsername(), user.getId(), ipAddress);
    }
    
    public void logPasswordChange(User user) {
        String ipAddress = getClientIpAddress();
        auditLogger.info("PASSWORD_CHANGED - User: {} (ID: {}), IP: {}", 
                        user.getUsername(), user.getId(), ipAddress);
    }
    
    public void logPasswordReset(User user) {
        String ipAddress = getClientIpAddress();
        auditLogger.info("PASSWORD_RESET - User: {} (ID: {}), IP: {}", 
                        user.getUsername(), user.getId(), ipAddress);
    }
    
    public void logUserDeactivation(User user) {
        String ipAddress = getClientIpAddress();
        auditLogger.info("USER_DEACTIVATED - User: {} (ID: {}), IP: {}", 
                        user.getUsername(), user.getId(), ipAddress);
    }
    
    public void logUserActivation(User user) {
        String ipAddress = getClientIpAddress();
        auditLogger.info("USER_ACTIVATED - User: {} (ID: {}), IP: {}", 
                        user.getUsername(), user.getId(), ipAddress);
    }
    
    public void logUserLogin(String username) {
        String ipAddress = getClientIpAddress();
        auditLogger.info("USER_LOGIN - Username: {}, IP: {}", username, ipAddress);
    }
    
    public void logUserLogout(String username) {
        String ipAddress = getClientIpAddress();
        auditLogger.info("USER_LOGOUT - Username: {}, IP: {}", username, ipAddress);
    }
    
    public void logFailedLogin(String username) {
        String ipAddress = getClientIpAddress();
        auditLogger.warn("LOGIN_FAILED - Username: {}, IP: {}", username, ipAddress);
    }
    
    private String getClientIpAddress() {
        if (request == null) {
            return "unknown";
        }
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
