package com.lab.model.config;

import com.lab.model.config.util.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        if (authorities.stream().anyMatch(authority -> Role.MANAGE_ACCOUNTS.name().equals(authority.getAuthority()))) {
            response.sendRedirect("/admin");
        } else  if (authorities.stream().anyMatch(authority -> Role.APPROVE_DAYS_OFF_REQUEST.name().equals(authority.getAuthority()))) {
            response.sendRedirect("/superior");
        } else if (authorities.stream().anyMatch(authority -> Role.AUTH.name().equals(authority.getAuthority()))) {
            response.sendRedirect("/employee");
        } else {
            logger.error("current user does not have a proper role");
            throw new IllegalStateException("Unexpected role: " + authorities);
        }
    }
}
