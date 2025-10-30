package org.example.do_an_v1.service.support;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Central place to resolve the acting user id.
 * Admins can target another user via the path parameter; everyone else must rely
 * on their own JWT claim. Missing or invalid JWT state causes the request to fail
 * fast so business logic stays clean.
 */
@Component
public class RequestIdentityResolver {

    public Long requireUserId(Long pathUserId) {
        Long resolved = resolveUserId(pathUserId);
        if (resolved == null) {
            throw new IllegalArgumentException("Unable to resolve user id from JWT");
        }
        return resolved;
    }

    private Long resolveUserId(Long pathUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            throw new IllegalArgumentException("JWT authentication is required");
        }

        boolean isAdmin = jwtAuthenticationToken.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> "ADMIN".equals(authority)
                        || "ROLE_ADMIN".equals(authority)
                        || "SUPER_ADMIN".equals(authority)
                        || "ROLE_SUPER_ADMIN".equals(authority));

        Jwt jwt = jwtAuthenticationToken.getToken();
        Object idClaim = jwt.getClaims().get("id");
        if (!(idClaim instanceof Number number)) {
            throw new IllegalArgumentException("JWT does not contain required user id claim");
        }
        Long jwtUserId = number.longValue();

        if (isAdmin && pathUserId != null) {
            return pathUserId;
        }

        return jwtUserId;
    }
}
