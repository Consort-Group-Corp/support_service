package uz.consortgroup.support_service.security;

import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;

import java.util.UUID;

public interface AuthContext {
    UUID getCurrentUserId();
    UserRole getCurrentUserRole();
}