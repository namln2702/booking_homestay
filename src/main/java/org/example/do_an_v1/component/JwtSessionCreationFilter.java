package org.example.do_an_v1.component;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

public class JwtSessionCreationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Nếu token hợp lệ -> authentication != null && đã xác thực
        if (authentication != null && authentication.isAuthenticated()) {

            // 🔥 Chỉ tạo session khi cần
            HttpSession session = request.getSession(false);
            if (session == null) {
                JWTClaimsSet claims = getClaims(request);
                session = request.getSession(true);

                session.setAttribute("id", claims.getClaim("id"));
                session.setAttribute("email", claims.getSubject());
                session.setAttribute("CREATED_AT", LocalDateTime.now());
                System.out.println(" Session created for user: " + authentication.getName());
            }
        }

        filterChain.doFilter(request, response);
    }

    public JWTClaimsSet getClaims(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);

                SignedJWT signedJWT = SignedJWT.parse(token);
                JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

                return claims;

            } catch (Exception e) {
                System.out.println("Token invalid in interceptor: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        throw new RuntimeException("Token error in ClaimSet");
    }
}
