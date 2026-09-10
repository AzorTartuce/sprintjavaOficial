package com.sprintjava.controller;

import com.sprintjava.controller.dto.LoginResponse;
import com.sprintjava.controller.dto.RegisterRequest;
import com.sprintjava.controller.dto.UpdateProfileRequest;
import com.sprintjava.controller.dto.UserResponse;
import com.sprintjava.dao.UserDAO;
import com.sprintjava.exception.ApiException;
import com.sprintjava.model.User;
import com.sprintjava.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserDAO userDAO, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/login", consumes = "application/x-www-form-urlencoded")
    public LoginResponse login(@RequestParam String username, @RequestParam String password) {
        try {
            User user = userDAO.buscarPorEmail(username);
            if (user == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
            }
            String token = jwtService.gerarToken(user.getId(), user.getEmail());
            return new LoginResponse(token, user);
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao autenticar usuário", e);
        }
    }

    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
        try {
            if (userDAO.buscarPorEmail(request.getEmail()) != null) {
                throw new ApiException(HttpStatus.CONFLICT, "E-mail já cadastrado");
            }
            User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()),
                    request.getFullName());
            user.setCompany(request.getCompany());
            user.setJobTitle(request.getJobTitle());
            user.setDepartment(request.getDepartment());
            user.setPhone(request.getPhone());
            user.setCity(request.getCity());
            user.setState(request.getState());
            user.setLinkedinUrl(request.getLinkedinUrl());
            user.setBio(request.getBio());

            if (!user.emailValido()) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "E-mail inválido");
            }

            userDAO.inserir(user);
            String token = jwtService.gerarToken(user.getId(), user.getEmail());
            return new LoginResponse(token, user);
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao cadastrar usuário", e);
        }
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return new UserResponse(usuarioAutenticado(authentication));
    }

    @PatchMapping("/me")
    public UserResponse atualizarPerfil(Authentication authentication, @RequestBody UpdateProfileRequest request) {
        User user = usuarioAutenticado(authentication);
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getCompany() != null) user.setCompany(request.getCompany());
        if (request.getJobTitle() != null) user.setJobTitle(request.getJobTitle());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getCity() != null) user.setCity(request.getCity());
        if (request.getState() != null) user.setState(request.getState());
        if (request.getLinkedinUrl() != null) user.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getBio() != null) user.setBio(request.getBio());

        try {
            userDAO.atualizar(user);
        } catch (SQLException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao atualizar perfil", e);
        }
        return new UserResponse(user);
    }

    private User usuarioAutenticado(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}
