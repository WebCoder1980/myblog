package org.myblog.users.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.myblog.users.appenum.RoleEnum;
import org.myblog.users.dto.request.LoginRequest;
import org.myblog.users.dto.request.SignupRequest;
import org.myblog.users.dto.request.UserPutRequest;
import org.myblog.users.dto.response.JwtResponse;
import org.myblog.users.exception.EmailIsAlreadyTakenException;
import org.myblog.users.exception.RestIllegalArgumentException;
import org.myblog.users.exception.UsernameIsAlreadyTakenException;
import org.myblog.users.kafka.event.UserCreatedEvent;
import org.myblog.users.kafka.producer.UserCreatedProducer;
import org.myblog.users.model.JwtInfo;
import org.myblog.users.model.OutboxModel;
import org.myblog.users.model.RoleModel;
import org.myblog.users.model.UserModel;
import org.myblog.users.repository.OutboxRepository;
import org.myblog.users.repository.RoleRepository;
import org.myblog.users.repository.UserRepository;
import org.myblog.users.service.security.UserDetailsImpl;
import org.myblog.users.service.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UsersService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserCreatedProducer userCreatedProducer;

    @Autowired
    private ObjectMapper objectMapper;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        String jwt = jwtUtils.generateJwtToken(authentication, roles);
        return new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }

    public UserModel registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new UsernameIsAlreadyTakenException();
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new EmailIsAlreadyTakenException();
        }

        UserModel user = new UserModel(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<RoleModel> roles = new HashSet<>();
        roles.add(roleRepository.findByName(RoleEnum.ROLE_USER).orElseThrow(() -> new RestIllegalArgumentException("Role is not found.")));

        user.setRoles(roles);

        OutboxModel outbox = new OutboxModel();

        outbox.setTopic(UserCreatedProducer.TOPIC_NAME);

        saveUserAndOutbox(user, outbox);

        UserCreatedEvent userCreatedEvent = new UserCreatedEvent(user);

        return user;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserModel> getAll() {
        return userRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserModel get(Integer id) {
        return userRepository.findById(id).orElseThrow(() -> new RestIllegalArgumentException("User was not found"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserModel put(Integer id,UserPutRequest request) {
        UserModel model = userRepository.findById(id)
                .orElseThrow(() -> new RestIllegalArgumentException("User was not found"));

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
                model.getRoles().add(roleRepository.findByName(RoleEnum.valueOf(i)).orElseThrow(() -> new RestIllegalArgumentException("Role was not found")));
            }
        }

        userRepository.save(model);

        return model;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Integer id) {
        UserModel model = userRepository.findById(id)
                .orElseThrow(() -> new RestIllegalArgumentException("User was not found"));

        userRepository.delete(model);
    }

    public JwtInfo decodeToken(String token) {
        return jwtUtils.getUserDataFromJwtToken(token);
    }

    @Transactional
    private void saveUserAndOutbox(UserModel userModel, OutboxModel outboxModel) {
        userRepository.save(userModel);

        outboxModel.setKey(userModel.getId());

        try {
            outboxModel.setValue(objectMapper.writeValueAsString(userModel));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(String.format("Unknown serialization error: %s", ex.getMessage()));
        }

        outboxRepository.save(outboxModel);
    }
}
