package uz.consortgroup.support_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.support_service.exception.UnauthorizedException;


import java.util.UUID;

@Component
public class AuthContextImpl implements AuthContext {

    @Override
    public UUID getCurrentUserId() {
        return getAuthenticatedUser().getUserId();
    }

    @Override
    public UserRole getCurrentUserRole() {
        return getAuthenticatedUser().getRole();
    }

    private AuthenticatedUser getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof AuthenticatedUser user)) {
            throw new UnauthorizedException("Invalid authentication principal");
        }

        return user;
    }
}