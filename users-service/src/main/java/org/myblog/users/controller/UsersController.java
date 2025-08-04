package org.myblog.users.controller;

import jakarta.validation.Valid;
import org.myblog.users.dto.request.UserPutRequest;
import org.myblog.users.appenum.RoleEnum;
import org.myblog.users.exception.RestIllegalArgumentException;
import org.myblog.users.model.JwtInfo;
import org.myblog.users.model.RoleModel;
import org.myblog.users.model.UserModel;
import org.myblog.users.dto.AppResponse;
import org.myblog.users.dto.request.LoginRequest;
import org.myblog.users.dto.request.SignupRequest;
import org.myblog.users.dto.response.JwtResponse;
import org.myblog.users.repository.RoleRepository;
import org.myblog.users.repository.UserRepository;
import org.myblog.users.service.UsersService;
import org.myblog.users.service.security.UserDetailsImpl;
import org.myblog.users.service.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private UsersService usersService;

    @PostMapping("/auth/login")
    public ResponseEntity<AppResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok().body(new AppResponse<JwtResponse>(usersService.authenticateUser(loginRequest)));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<AppResponse<UserModel>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return ResponseEntity.ok().body(new AppResponse<UserModel>(usersService.registerUser(signUpRequest)));
    }

    @GetMapping("/user")
    public ResponseEntity<AppResponse<List<UserModel>>> getAll() {
        return ResponseEntity.ok().body(new AppResponse<List<UserModel>>(usersService.getAll()));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<AppResponse<UserModel>> get(@PathVariable Integer id) {
        return ResponseEntity.ok().body(new AppResponse<UserModel>(usersService.get(id)));
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<AppResponse<UserModel>> put(@PathVariable Integer id, @Valid @RequestBody UserPutRequest request) {
        return ResponseEntity.ok().body(new AppResponse<>(usersService.put(id, request)));
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<AppResponse<String>> delete(@PathVariable Integer id) {
        usersService.delete(id);

        return ResponseEntity.ok().body(new AppResponse<>("Deleted"));
    }
}
