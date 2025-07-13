package org.myblog.users.controller;

import jakarta.validation.Valid;
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
                    .body(new AppResponse<UserModel>().addErrorFluent("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new AppResponse<UserModel>().addErrorFluent("Error: Email is already in use!"));
        }

        UserModel user = new UserModel(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<RoleModel> roles = new HashSet<>();
        roles.add(roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found.")));

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok().body(new AppResponse<UserModel>(user));
    }

    @GetMapping("/token/decode")
    public ResponseEntity<AppResponse<JwtInfo>> decodeToken(@RequestParam("token") String token) {
        return ResponseEntity.ok().body(new AppResponse<>(jwtUtils.getUserDataFromJwtToken(token)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleError(MethodArgumentNotValidException ex) {
        AppResponse<?> result = new AppResponse<>(new TreeMap<>());

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(i -> {
                    result.getErrors().computeIfAbsent(i.getField(), j -> new TreeSet<>());
                    result.getErrors().get(i.getField()).add(i.getDefaultMessage());
                });

        return ResponseEntity.badRequest().body(result);
    }
}
