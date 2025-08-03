package org.myblog.users.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myblog.users.appenum.RoleEnum;
import org.myblog.users.dto.request.LoginRequest;
import org.myblog.users.dto.request.SignupRequest;
import org.myblog.users.dto.request.UserPutRequest;
import org.myblog.users.dto.response.JwtResponse;
import org.myblog.users.exception.EmailIsAlreadyTakenException;
import org.myblog.users.exception.RestIllegalArgumentException;
import org.myblog.users.exception.UsernameIsAlreadyTakenException;
import org.myblog.users.model.JwtInfo;
import org.myblog.users.model.JwtUserInfo;
import org.myblog.users.model.RoleModel;
import org.myblog.users.model.UserModel;
import org.myblog.users.repository.RoleRepository;
import org.myblog.users.repository.UserRepository;
import org.myblog.users.service.security.UserDetailsImpl;
import org.myblog.users.service.security.jwt.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsersServiceUnitTests {
    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private UsersService usersService;

    @Test
    public void authenticateUser_Ok() {
        // Arrange

        String expectedToken = "aaa";
        Integer expectedId = 1;
        String expectedUsername = "maxsmg";
        String expectedPassword = "qweqwe";
        String expectedEmail = "maxprogger@mail.ru";
        List<String> expectedRoles = Arrays.asList(RoleEnum.ROLE_USER.toString());

        JwtResponse expectedJwtResponse = new JwtResponse(expectedToken, expectedId, expectedUsername, expectedEmail, expectedRoles);

        LoginRequest input = new LoginRequest();
        input.setUsername(expectedUsername);
        input.setPassword(expectedPassword);

        UserDetails userDetails = new UserDetailsImpl(expectedId, expectedUsername, expectedEmail, "encoded", Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(any(Authentication.class), anyList())).thenReturn(expectedToken      );

        // Act

        JwtResponse actual = usersService.authenticateUser(input);

        // Assert

        assertEquals(expectedJwtResponse, actual);

        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(authentication).getPrincipal();
        verify(jwtUtils).generateJwtToken(any(Authentication.class), anyList());
    }

    @Test
    public void authenticateUser_InvalidLoginOrPassword() {
        // Arrange

        LoginRequest input = new LoginRequest();
        input.setUsername("maxsmg");
        input.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert

        assertThrows(BadCredentialsException.class, () -> usersService.authenticateUser(input));

        // Assert

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authentication, never()).getPrincipal();
        verify(jwtUtils, never()).generateJwtToken(any(Authentication.class), anyList());
    }

    @Test
    public void registerUser_Ok() {
        // Arrange

        Integer expectedId = 1;
        String expectedUsername = "newUser";
        String expectedPassword = "123456789";
        String expectedEncodedPassword = "encoded";
        String expectedEmail = "newUser@mail.com";

        RoleModel expectedRoleModel = new RoleModel();
        expectedRoleModel.setId(expectedId);
        expectedRoleModel.setName(RoleEnum.ROLE_USER);

        SignupRequest input = new SignupRequest();
        input.setUsername(expectedUsername);
        input.setPassword(expectedPassword);
        input.setEmail(expectedEmail);

        UserModel expected = new UserModel();
        expected.setId(1);
        expected.setUsername(expectedUsername);
        expected.setPassword(expectedEncodedPassword);
        expected.setEmail(expectedEmail);
        expected.setRoles(Set.of(expectedRoleModel));

        when(userRepository.existsByUsername(any(String.class))).thenReturn(false);
        when(userRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(encoder.encode(any(String.class))).thenReturn(expectedEncodedPassword);
        when(roleRepository.findByName(any(RoleEnum.class))).thenReturn(Optional.of(expectedRoleModel));
        when(userRepository.save(any(UserModel.class))).thenAnswer(i -> {
            UserModel result = i.getArgument(0);
            result.setId(expectedId);
            return result;
        });

        // Act

        UserModel actual = usersService.registerUser(input);

        // Assert

        assertEquals(expected, actual);

        verify(userRepository).existsByUsername(expectedUsername);
        verify(userRepository).existsByEmail(expectedEmail);
        verify(encoder).encode(expectedPassword);
        verify(roleRepository).findByName(RoleEnum.ROLE_USER);
    }

    @Test
    public void registerUser_UsernameIsTaken() {
        // Arrange

        String expectedUsername = "newUser";
        String expectedPassword = "123456789";
        String expectedEmail = "newUser@mail.com";

        SignupRequest input = new SignupRequest();
        input.setUsername(expectedUsername);
        input.setPassword(expectedPassword);
        input.setEmail(expectedEmail);

        when(userRepository.existsByUsername(any(String.class))).thenThrow(new UsernameIsAlreadyTakenException());

        // Act & Assert

        assertThrows(UsernameIsAlreadyTakenException.class, () -> usersService.registerUser(input));

        // Assert

        verify(userRepository).existsByUsername(expectedUsername);
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    public void registerUser_EmailIsTaken() {
        // Arrange

        String expectedUsername = "newUser";
        String expectedPassword = "123456789";
        String expectedEmail = "newUser@mail.com";

        SignupRequest input = new SignupRequest();
        input.setUsername(expectedUsername);
        input.setPassword(expectedPassword);
        input.setEmail(expectedEmail);

        when(userRepository.existsByUsername(any(String.class))).thenReturn(false);
        when(userRepository.existsByEmail(any(String.class))).thenThrow(new EmailIsAlreadyTakenException());

        // Act & Assert

        assertThrows(EmailIsAlreadyTakenException.class, () -> usersService.registerUser(input));

        // Assert

        verify(userRepository).existsByUsername(expectedUsername);
        verify(userRepository).existsByEmail(expectedEmail);
        verify(userRepository, never()).save(any(UserModel.class));
    }

    @Test
    public void getAll_Ok() {
        // Arrange

        List<UserModel> expected = new ArrayList<>();

        RoleModel expectedRoleModel = new RoleModel();
        expectedRoleModel.setId(1);
        expectedRoleModel.setName(RoleEnum.ROLE_USER);

        UserModel user1 = new UserModel();
        user1.setId(1);
        user1.setUsername("testUser1");
        user1.setPassword("123456789");
        user1.setEmail("testUser1@mail.com");
        user1.setRoles(Set.of(expectedRoleModel));

        UserModel user2 = new UserModel();
        user2.setId(2);
        user2.setUsername("testUser2");
        user2.setPassword("123456789");
        user2.setEmail("testUser2@mail.com");
        user2.setRoles(Set.of(expectedRoleModel));

        expected.add(user1);
        expected.add(user2);

        when(userRepository.findAll()).thenReturn(expected);

        // Act

        List<UserModel> actual = usersService.getAll();

        // Assert

        assertEquals(expected, actual);

        verify(userRepository).findAll();
    }

    @Test
    public void get_Ok() {
        // Arrange

        RoleModel expectedRoleModel = new RoleModel();
        expectedRoleModel.setId(1);
        expectedRoleModel.setName(RoleEnum.ROLE_USER);

        UserModel expected = new UserModel();
        expected.setId(1);
        expected.setUsername("testUser1");
        expected.setPassword("123456789");
        expected.setEmail("testUser1@mail.com");
        expected.setRoles(Set.of(expectedRoleModel));

        when(userRepository.findById(any(Integer.class))).thenReturn(Optional.of(expected));

        // Act

        UserModel actual = usersService.get(expected.getId());

        // Assert

        assertEquals(expected, actual);

        verify(userRepository).findById(expected.getId());
    }

    @Test
    public void put_Ok() {
        // Arrange

        Integer userId = 1;

        RoleModel expectedRoleModel = new RoleModel();
        expectedRoleModel.setId(2);
        expectedRoleModel.setName(RoleEnum.ROLE_MODERATOR);

        UserModel expected = new UserModel();
        expected.setId(userId);
        expected.setUsername("newUsername");
        expected.setPassword("newPassword");
        expected.setEmail("newEmail@mail.com");
        expected.setRoles(new HashSet<>(Set.of(expectedRoleModel)));

        RoleModel actualRoleModel = new RoleModel();
        actualRoleModel.setId(1);
        actualRoleModel.setName(RoleEnum.ROLE_USER);

        UserModel actualMock = new UserModel();
        actualMock.setId(userId);
        actualMock.setUsername("oldUsername");
        actualMock.setPassword("oldPassword");
        actualMock.setEmail("oldMail@mail.com");
        actualMock.setRoles(new HashSet<>(Set.of(actualRoleModel)));

        when(userRepository.findById(any(Integer.class))).thenReturn(Optional.of(actualMock));
        when(roleRepository.findByName(any(RoleEnum.class))).thenAnswer(i -> {
            RoleEnum roleEnum = i.getArgument(0);

            RoleModel result = new RoleModel();

            if (roleEnum.equals(RoleEnum.ROLE_USER)) {
                result.setId(1);
            } else if (roleEnum.equals(RoleEnum.ROLE_MODERATOR)) {
                result.setId(2);
            } else if (roleEnum.equals(RoleEnum.ROLE_ADMIN)) {
                result.setId(3);
            }

            result.setName(roleEnum);

            return Optional.of(result);
        });
        when(encoder.encode(any(String.class))).thenReturn(expected.getPassword());

        UserPutRequest userPutRequest = new UserPutRequest();
        userPutRequest.setUsername(expected.getUsername());
        userPutRequest.setPassword(expected.getPassword());
        userPutRequest.setEmail(expected.getEmail());
        userPutRequest.setRoles(new HashSet<>(expected.getRoles()
                .stream()
                .map(i -> i.getName().toString())
                .toList()
        ));

        // Act

        UserModel actual = usersService.put(userId, userPutRequest);

        // Assert

        assertEquals(expected, actual);

        verify(userRepository).findById(actual.getId());
        verify(roleRepository).findByName(any(RoleEnum.class));
        verify(encoder).encode(any(String.class));
    }

    @Test
    public void put_OkEmpty() {
        // Arrange

        Integer userId = 1;

        RoleModel actualRoleModel = new RoleModel();
        actualRoleModel.setId(1);
        actualRoleModel.setName(RoleEnum.ROLE_USER);

        UserModel actualMock = new UserModel();
        actualMock.setId(userId);
        actualMock.setUsername("oldUsername");
        actualMock.setPassword("oldPassword");
        actualMock.setEmail("oldMail@mail.com");
        actualMock.setRoles(new HashSet<>(Set.of(actualRoleModel)));

        UserModel expected = new UserModel();
        expected.setId(userId);
        expected.setUsername(actualMock.getUsername());
        expected.setPassword(actualMock.getPassword());
        expected.setEmail(actualMock.getEmail());
        expected.setRoles(actualMock.getRoles());

        when(userRepository.findById(any(Integer.class))).thenReturn(Optional.of(actualMock));

        UserPutRequest userPutRequest = new UserPutRequest();

        // Act

        UserModel actual = usersService.put(userId, userPutRequest);

        // Assert

        assertEquals(expected, actual);

        verify(userRepository).findById(actual.getId());
        verify(roleRepository, never()).findByName(any(RoleEnum.class));
        verify(encoder, never()).encode(any(String.class));
    }

    @Test
    public void delete_Ok() {
        // Arrange

        Integer id = 1;

        UserModel userModel = new UserModel();

        when(userRepository.findById(any(Integer.class))).thenReturn(Optional.of(userModel));

        // Act

        usersService.delete(id);

        // Assert

        verify(userRepository).findById(id);
        verify(userRepository).delete(any(UserModel.class));
    }

    @Test
    public void delete_UserIsNotFound() {
        // Arrange

        Integer id = 1;

        UserModel userModel = new UserModel();

        // Act && Assert

        assertThrows(RestIllegalArgumentException.class, () -> usersService.delete(id));

        // Assert

        verify(userRepository).findById(id);
        verify(userRepository, never()).delete(any(UserModel.class));
    }

    @Test
    public void decodeToken_Ok() {
        // Arrange

        JwtInfo expected = new JwtInfo();
        expected.setExpiration(new Date(LocalDate.now().getYear()));
        expected.setUserInfo(new JwtUserInfo());
        expected.getUserInfo().setId(1);

        String token = "encrypted";

        when(jwtUtils.getUserDataFromJwtToken(any(String.class))).thenReturn(expected);

        // Act

        JwtInfo actual = usersService.decodeToken(token);

        // Assert

        assertEquals(expected, actual);

        verify(jwtUtils).getUserDataFromJwtToken(any(String.class));
    }

    @Test
    public void decodeToken_BrokenToken() {
        // Arrange

        String token = "encrypted";

        when(jwtUtils.getUserDataFromJwtToken(any(String.class))).thenThrow(new RuntimeException());

        // Act && Assure

        assertThrows(RuntimeException.class, () -> usersService.decodeToken(token));

        // Assert

        verify(jwtUtils).getUserDataFromJwtToken(any(String.class));
    }
}