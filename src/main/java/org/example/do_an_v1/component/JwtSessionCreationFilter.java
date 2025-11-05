package org.example.do_an_v1.component;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

public class JwtSessionCreationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtSessionCreationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            HttpSession session = request.getSession(false);
            if (session == null) {
                JWTClaimsSet claims = resolveClaimsOrFail(request, response);
                if (claims == null) {
                    return;
                }
                session = request.getSession(true);
                session.setAttribute("id", claims.getClaim("id"));
                session.setAttribute("email", claims.getSubject());
                session.setAttribute("CREATED_AT", LocalDateTime.now());
                log.debug("Session created for user: {}", authentication.getName());
            }
        }

        filterChain.doFilter(request, response);
    }

    private JWTClaimsSet resolveClaimsOrFail(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                SignedJWT signedJWT = SignedJWT.parse(token);
                return signedJWT.getJWTClaimsSet();
            } catch (Exception ex) {
                log.warn("Token invalid in interceptor: {}", ex.getMessage());
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid bearer token");
                return null;
            }
        }
        log.debug("Missing or malformed Authorization header for request {}", request.getRequestURI());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization header missing or malformed");
        return null;
    }
}
