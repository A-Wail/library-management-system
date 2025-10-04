package com.task.library_managment_system.service;

import com.task.library_managment_system.dto.systemUser.AuthenticationRequest;
import com.task.library_managment_system.dto.systemUser.AuthenticationResp;
import com.task.library_managment_system.dto.systemUser.RequestUser;
import com.task.library_managment_system.dto.systemUser.UserResponse;
import com.task.library_managment_system.exception.EntityFoundException;
import com.task.library_managment_system.exception.EntityNotFoundException;
import com.task.library_managment_system.models.SystemUser;
import com.task.library_managment_system.repository.SystemUserRepo;
import com.task.library_managment_system.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock private  SystemUserRepo userRepo;
    @Mock private  PasswordEncoder passwordEncoder;
    @Mock private  JwtService jwtService;
    @Mock private  AuthenticationManager authenticationManager;
    @Mock private Authentication authentication;
    @InjectMocks private UserServiceImpl userService;
    private SystemUser user;
    private RequestUser requestUser;
    private AuthenticationRequest authRequest;

    @BeforeEach
    void setUp() {
        user=SystemUser.builder()
                .id(1L)
                .email("abdo@gmail.com")
                .username("abdo")
                .hashPass("EncodedPassword")
                .role(SystemUser.Role.ADMIN)
                .build();
        requestUser=RequestUser.builder()
                .email("abdo@gmail.com")
                .username("abdo")
                .password("password123")
                .role(SystemUser.Role.ADMIN)
                .build();
        authRequest = AuthenticationRequest.builder()
                .username("abdo")
                .password("password123")
                .build();
    }

    @Test
    void registerSuccess() {
        //when
        when(userRepo.findByUsername("abdo")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("EncodedPassword");
        when(jwtService.generateToken(any(SystemUser.class))).thenReturn("JwtToken");
        when(userRepo.save(any(SystemUser.class))).thenReturn(user);
        //then
        AuthenticationResp resp=userService.register(requestUser);
        //assert
        assertNotNull(resp,"Response shouldn't be null");
        assertEquals("JwtToken",resp.getToken(),"Token should be match!");
        verify(userRepo,times(1)).findByUsername("abdo");
        verify(userRepo,times(1)).save(any(SystemUser.class));
        verify(jwtService,times(1)).generateToken(any(SystemUser.class));
    }

    @Test
    void registerThrowEntityFoundException_whenUserRegisterBefore() {
        //when
        when(userRepo.findByUsername("abdo")).thenReturn(Optional.of(user));
        //then
        EntityFoundException exception=assertThrows(EntityFoundException.class,
                () -> userService.register(requestUser),
                "Should throw exception as user register before!");

        assertEquals("User is register before!", exception.getMessage());
        verify(userRepo, times(1)).findByUsername("abdo");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepo, never()).save(any(SystemUser.class));
        verify(jwtService, never()).generateToken(any(SystemUser.class));
    }

    @Test
    void authenticateSuccess() {
        //when
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepo.findByUsername("abdo")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(SystemUser.class))).thenReturn("jwtToken");

        // Act
        AuthenticationResp response = userService.authenticate(authRequest);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals("jwtToken", response.getToken(), "Token should match");
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepo, times(1)).findByUsername("abdo");
        verify(jwtService, times(1)).generateToken(any(SystemUser.class));
    }

    @Test
    void authenticateThrowsEntityNotFoundException_whenUserNotFound() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepo.findByUsername("abdo")).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.authenticate(authRequest),
                "Should throw exception as user not found!");
        assertEquals("User not found !!", exception.getMessage());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepo, times(1)).findByUsername("abdo");
        verify(jwtService, never()).generateToken(any(SystemUser.class));
    }

    @Test
    void createUser_success() {
        // given
        when(userRepo.findByUsername("abdo")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("abdo@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepo.save(any(SystemUser.class))).thenReturn(user);

        // then
        UserResponse response = userService.createUser(requestUser);

        // ASSERT
        assertNotNull(response, "Response should not be null");
        assertEquals("abdo", response.getUsername(), "Username should match");
        assertEquals("abdo@gmail.com", response.getEmail(), "Email should match");
        assertEquals("ADMIN", response.getRole().name(), "Role should match");
        verify(userRepo, times(1)).findByUsername("abdo"); // Called twice in createUser
        verify(userRepo, times(1)).findByEmail("abdo@gmail.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepo, times(1)).save(any(SystemUser.class));
    }

    @Test
    void createUser_throwsEntityFoundException_whenUsernameExists() {
        // Arrange
        when(userRepo.findByUsername("abdo")).thenReturn(Optional.of(user));

        // Act & Assert
        EntityFoundException exception = assertThrows(EntityFoundException.class,
                () -> userService.createUser(requestUser),
                "Should throw EntityFoundException as username exist!");
        assertEquals("Username already exist !!", exception.getMessage());
        verify(userRepo, times(1)).findByUsername("abdo");
        verify(userRepo, never()).findByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepo, never()).save(any(SystemUser.class));
    }

    @Test
    void createUser_throwsEntityFoundException_whenEmailExists() {
        // Arrange
        when(userRepo.findByUsername("abdo")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("abdo@gmail.com")).thenReturn(Optional.of(user));

        // Act & Assert
        EntityFoundException exception = assertThrows(EntityFoundException.class,
                () -> userService.createUser(requestUser),
                "Should throw exception as email used!");
        assertEquals("Email already exist !!", exception.getMessage());
        verify(userRepo, times(1)).findByUsername("abdo");
        verify(userRepo, times(1)).findByEmail("abdo@gmail.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepo, never()).save(any(SystemUser.class));
    }

    @Test
    void getAllUsers_success() {
        // Arrange
        SystemUser user2 = SystemUser.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .role(SystemUser.Role.LIBRARIAN)
                .build();
        when(userRepo.findAll()).thenReturn(Arrays.asList(user, user2));

        // Act
        List<UserResponse> responses = userService.getAllUsers();

        // Assert
        assertEquals(2, responses.size(), "Should return two users");
        assertEquals("abdo", responses.get(0).getUsername(), "First username should match");
        assertEquals("user2", responses.get(1).getUsername(), "Second username should match");
        verify(userRepo, times(1)).findAll();
    }

    @Test
    void returnUserById_success() {
        // Arrange
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        // Act
        UserResponse response = userService.returnUserById(1L);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals("abdo", response.getUsername(), "Username should match");
        assertEquals("abdo@gmail.com", response.getEmail(), "Email should match");
        assertEquals("ADMIN", response.getRole().name(), "Role should match");
        verify(userRepo, times(1)).findById(1L);
    }

    @Test
    void returnUserById_throwsEntityNotFoundException_whenUserNotFound() {
        // Arrange
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.returnUserById(1L),
                "Should throw exception as user not exist!");
        assertEquals("User not found:1", exception.getMessage());
        verify(userRepo, times(1)).findById(1L);
    }

}