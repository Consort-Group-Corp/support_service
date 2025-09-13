package uz.consortgroup.support_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
public class JwtUtils {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USER_TYPE = "userType";

    @Value("${security.token}")
    private String jwtSecret;

    @Value("${security.expiration}")
    private long jwtExpirationMs;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Failed to parse JWT claims: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    private boolean isTokenValid(Claims claims) {
        try {
            Date exp = claims.getExpiration();
            if (exp != null) {
                return exp.after(new Date());
            }

            Date iat = claims.getIssuedAt();
            if (iat == null) {
                log.error("JWT has neither exp nor iat");
                return false;
            }

            long computedExpMs = iat.getTime() + jwtExpirationMs;
            return computedExpMs > System.currentTimeMillis();
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateToken(String authToken) {
        try {
            Claims claims = getClaims(authToken);
            return isTokenValid(claims);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during token validation: {}", e.getMessage());
        }
        return false;
    }

    public boolean isValidToken(String token) {
        return validateToken(token);
    }

    private Claims getValidatedClaims(String token) {
        Claims claims = getClaims(token);
        if (!isTokenValid(claims)) {
            throw new SecurityException("Invalid or expired token");
        }
        return claims;
    }

    public UUID getUserIdFromToken(String token) {
        try {
            Claims claims = getValidatedClaims(token);
            String userIdStr = claims.get(CLAIM_USER_ID, String.class);
            return userIdStr != null ? UUID.fromString(userIdStr) : null;
        } catch (IllegalArgumentException e) {
            log.error("Invalid userId format in token: {}", e.getMessage());
            return null;
        }
    }

    public String getUserRoleFromToken(String token) {
        Claims claims = getValidatedClaims(token);
        return claims.get(CLAIM_USER_TYPE, String.class);
    }
}