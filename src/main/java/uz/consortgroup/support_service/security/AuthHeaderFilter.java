package uz.consortgroup.support_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.consortgroup.core.api.v1.dto.user.enumeration.UserRole;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class AuthHeaderFilter extends OncePerRequestFilter {

    public static final String ATTR_USER_ID    = "auth.userId";
    public static final String ATTR_USER_ROLES = "auth.userRoles";
    public static final String ATTR_VALIDATED  = "auth.validated";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        boolean validated = "true".equalsIgnoreCase(req.getHeader(AuthHeaders.AUTH_VALIDATED));
        String userIdStr  = trimOrNull(req.getHeader(AuthHeaders.USER_ID));
        String rolesHdr   = trimOrNull(req.getHeader(AuthHeaders.ROLES));

        if (validated && userIdStr != null) {
            UUID userId = parseUuid(userIdStr);
            if (userId != null) {
                req.setAttribute(ATTR_VALIDATED, Boolean.TRUE);
                req.setAttribute(ATTR_USER_ID, userId);
                List<UserRole> roles = parseRoles(rolesHdr);
                if (!roles.isEmpty()) {
                    req.setAttribute(ATTR_USER_ROLES, roles);
                }
            }
        }

        chain.doFilter(req, res);
    }

    private static String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static UUID parseUuid(String s) {
        try { return UUID.fromString(s); } catch (Exception e) { return null; }
    }

    private static List<UserRole> parseRoles(String hdr) {
        List<UserRole> out = new ArrayList<>();
        if (hdr == null) return out;
        for (String p : hdr.split("[,;\\s]+")) {
            if (p.isBlank()) continue;
            try {
                out.add(UserRole.valueOf(p.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) { /* неизвестная роль — пропускаем */ }
        }
        return out;
    }
}