package org.example.do_an_v1.component;

import com.google.auth.oauth2.JwtClaims;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.StandardCharsets;
import java.util.*;


@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Value("${jwt.signer-key}")
    private String SIGNER_KEY;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object){

        HttpSession session = request.getSession(false);
        if(Objects.isNull(session) || session.getId() == null){
//
//            JWTClaimsSet claims = getClaims(request);
//            session = request.getSession(true);
//            session.setAttribute("id", claims.getClaim("id"));
//            session.setAttribute("email", claims.getSubject());
        }


        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handle, ModelAndView modelAndView){
        SecurityContextHolder.clearContext();
    }



}
