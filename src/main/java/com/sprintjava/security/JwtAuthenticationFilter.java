package com.sprintjava.security;

import com.sprintjava.dao.UserDAO;
import com.sprintjava.model.User;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lê o header "Authorization: Bearer <token>", resolve o usuário e popula o
 * SecurityContext. Requisições sem token ou com token inválido seguem sem
 * autenticação — a decisão de exigir 401 fica com o SecurityConfig.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDAO userDAO;

    public JwtAuthenticationFilter(JwtService jwtService, UserDAO userDAO) {
        this.jwtService = jwtService;
        this.userDAO = userDAO;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length());
            try {
                Long userId = jwtService.extrairUserId(token);
                User user = userDAO.buscarPorId(userId);
                if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (JwtException | IllegalArgumentException | java.sql.SQLException ignored) {
                // token inválido/expirado: segue sem autenticação, SecurityConfig responde 401
            }
        }
        filterChain.doFilter(request, response);
    }
}
