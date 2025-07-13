package org.myblog.users.controller;

import jakarta.validation.Valid;
import org.myblog.users.dto.request.UserPutRequest;
import org.myblog.users.model.ERole;
import org.myblog.users.model.JwtInfo;
import org.myblog.users.model.RoleModel;
import org.myblog.users.model.UserModel;
import org.myblog.users.dto.AppResponse;
import org.myblog.users.dto.request.LoginRequest;
import org.myblog.users.dto.request.SignupRequest;
import org.myblog.users.dto.response.JwtResponse;
import org.myblog.users.repository.RoleRepository;
import org.myblog.users.repository.UserRepository;
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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UsersController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/auth/login")
    public ResponseEntity<AppResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        String jwt = jwtUtils.generateJwtToken(authentication, roles);


        return ResponseEntity.ok(new AppResponse(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles)));
    }

    @PostMapping("/auth/register")
    public ResponseEntity<AppResponse<UserModel>> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new AppResponse<UserModel>().addErrorFluent("Username is already taken"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new AppResponse<UserModel>().addErrorFluent("Email is already in use"));
        }

        UserModel user = new UserModel(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<RoleModel> roles = new HashSet<>();
        roles.add(roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Role is not found.")));

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok().body(new AppResponse<UserModel>(user));
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse<List<UserModel>>> getAll() {
        return ResponseEntity.ok().body(new AppResponse<List<UserModel>>(userRepository.findAll()));
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse<UserModel>> get(@PathVariable Integer id) {
        return ResponseEntity.ok().body(new AppResponse<UserModel>(userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User was not fount"))));
    }

    @PutMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse<UserModel>> put(@PathVariable Integer id, @Valid @RequestBody UserPutRequest request) {
        UserModel model = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User was not found"));

        if (request.getUsername() != null) {
            model.setUsername(request.getUsername());
        }

        if (request.getPassword() != null) {
            model.setPassword(encoder.encode(request.getPassword()));
        }

        if (request.getEmail() != null) {
            model.setEmail(request.getEmail());
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            model.getRoles().clear();

            for (String i : request.getRoles()) {
                model.getRoles().add(roleRepository.findByName(ERole.valueOf(i)).orElseThrow(() -> new IllegalArgumentException("Role was not found")));
            }
        }

        userRepository.save(model);

        return ResponseEntity.ok().body(new AppResponse<>(model));
    }

    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppResponse<String>> delete(@PathVariable Integer id) {
        UserModel model = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User was not found"));

        userRepository.delete(model);

        return ResponseEntity.ok().body(new AppResponse<>("Deleted"));
    }

    @GetMapping("/token/decode")
    public ResponseEntity<AppResponse<JwtInfo>> decodeToken(@RequestParam("token") String token) {
        return ResponseEntity.ok().body(new AppResponse<>(jwtUtils.getUserDataFromJwtToken(token)));
    }
}
