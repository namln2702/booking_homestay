package org.example.do_an_v1.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class SessionConfig {

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public HttpSession httpSession() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Nếu token hợp lệ -> authentication != null && đã xác thực
        if (authentication != null && authentication.isAuthenticated()) {
            return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession();
        }
        throw new RuntimeException("Not created session");

    }
}
