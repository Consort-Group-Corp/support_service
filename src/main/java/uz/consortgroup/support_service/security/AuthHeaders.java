package uz.consortgroup.support_service.security;

public final class AuthHeaders {
    private AuthHeaders() {}
    public static final String USER_ID = "X-User-Id";
    public static final String ROLES   = "X-User-Roles";
    public static final String AUTH_VALIDATED = "X-Auth-Validated";
    public static final String REQUEST_ID = "X-Request-Id";
}