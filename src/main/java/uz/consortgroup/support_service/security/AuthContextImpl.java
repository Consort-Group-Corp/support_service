package uz.consortgroup.support_service.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;
import uz.consortgroup.support_service.exception.UnauthorizedException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
class AuthContextImpl implements AuthContext {

    private final HttpServletRequest request;

    @Override
    public UUID getCurrentUserId() {
        Object id = request.getAttribute(AuthHeaderFilter.ATTR_USER_ID);

        if (id instanceof UUID uuid) return uuid;
        throw new UnauthorizedException("User is not authenticated");
    }

    @Override
    public UserRole getCurrentUserRole() {
        String rolesHeader = request.getHeader(AuthHeaders.ROLES);
        if (rolesHeader != null && !rolesHeader.isBlank()) {
            String first = rolesHeader.split(",")[0].trim();
            try {
                return UserRole.valueOf(first);
            } catch (IllegalArgumentException ignored) {
            }
        }
        throw new UnauthorizedException("User role is missing");
    }
}