package uz.consortgroup.support_service.security;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class AuthenticatedUser {
    private final UUID userId;
    private final UserRole role;
}