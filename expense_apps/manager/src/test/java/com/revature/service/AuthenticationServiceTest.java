package com.revature.service;


import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.revature.repository.User;
import com.revature.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
 class AuthenticationServiceTest {
    @Mock
    UserRepository userRepository;

    User user;

    AuthenticationService authenticationService;

    @BeforeEach
     void setup() {
        user = new User();
        authenticationService = new AuthenticationService(userRepository);
    }

    @Test
     void createJwtTokenSuccessTest() {
        user.setId(1);
        user.setUsername("username");
        user.setRole("manager");

        String token = authenticationService.createJwtToken(user);

        assertNotNull(token);

        DecodedJWT decoded = JWT.decode(token);

        assertEquals("username", decoded.getClaim("username").asString());
        assertEquals("manager", decoded.getClaim("role").asString());

        Instant issue = decoded.getIssuedAtAsInstant();
        Instant expire = decoded.getExpiresAtAsInstant();

        assertEquals(issue.plus(24, ChronoUnit.HOURS), expire);
    }

    @Test
     void validateJwtTokenSuccessTest() {
        user.setId(1);
        user.setUsername("username");
        user.setRole("manager");

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));

        String token = authenticationService.createJwtToken(user);
        Optional<User> userOptional = authenticationService.validateJwtToken(token);

        assertTrue(userOptional.isPresent());
        verify(userRepository).findById(1);
    }

    @ParameterizedTest
    //@NullSource
    @ValueSource(strings = {"", "  ", "\n", "   \n"})
     void validateJwtTokenNoTokenTest(String strings) {
        Optional<User> userOptional = authenticationService.validateJwtToken(strings);
        assertFalse(userOptional.isPresent());
    }

    @Test
     void validateJwtTokenInvalidTokenTest() {
        Optional<User> userOptional = authenticationService.validateJwtToken("fake token");
        assertFalse(userOptional.isPresent());
    }

    @Test
     void validateJwtTokenInvalidIDTest() {
        when(userRepository.findById(anyInt())).thenThrow(NumberFormatException.class);

        String token = authenticationService.createJwtToken(user);
        Optional<User> userOptional = authenticationService.validateJwtToken(token);
        assertFalse(userOptional.isPresent());
    }

    @Test
     void validateAuthenticationSuccessTest() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        Optional<User> newUser = authenticationService.validateAuthentication("Bearer 1");
        assertTrue(newUser.isPresent());
    }

    @ParameterizedTest
    //@NullSource
    @ValueSource(strings = {"NotBearer 1", "Bearer StringNotInt", "Bearer 1.1", "Bearer "})
     void validateAuthenticationInvalidBearerTest(String strings) {
        Optional<User> newUser = authenticationService.validateAuthentication(strings);
        assertFalse(newUser.isPresent());
    }

    @Test
     void isManagerTrueTest() {
        user.setRole("manager");
        assertTrue(authenticationService.isManager(user));
    }

    @Test
     void isManagerFalseTest() {
        user.setRole("employee");
        assertFalse(authenticationService.isManager(user));
        assertFalse(authenticationService.isManager(null));
    }

    @Test
     void validateManagerAuthenticationSuccessTest() {
        user.setId(1);
        user.setUsername("username");
        user.setRole("manager");

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        String token = authenticationService.createJwtToken(user);
        Optional<User> userOptional = authenticationService.validateManagerAuthentication(token);
        assertTrue(userOptional.isPresent());
        verify(userRepository).findById(anyInt());
    }

    @Test
     void validateManagerAuthenticationInvalidTokenTest() {
        Optional<User> userOptional = authenticationService.validateManagerAuthentication("token");
        assertFalse(userOptional.isPresent());

        //userRepo fails
        user.setId(1);
        user.setUsername("username");
        user.setRole("manager");

        String token = authenticationService.createJwtToken(user);
        userOptional = authenticationService.validateManagerAuthentication(token);
        assertFalse(userOptional.isPresent());
    }

    @Test
     void validateManagerAuthenticationNotManagerTest() {
        user.setId(1);
        user.setUsername("username");
        user.setRole("employee");

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        String token = authenticationService.createJwtToken(user);
        Optional<User> userOptional = authenticationService.validateManagerAuthentication(token);
        assertFalse(userOptional.isPresent());
        verify(userRepository).findById(anyInt());
    }
    @Test
     void validateManagerAuthenticationLegacySuccessTest() {
        user.setId(1);
        user.setUsername("username");
        user.setRole("manager");

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        Optional<User> userOptional = authenticationService.validateManagerAuthenticationLegacy("Bearer 1");
        assertTrue(userOptional.isPresent());
        verify(userRepository).findById(anyInt());
    }

    @ParameterizedTest
    //@NullSource
    @ValueSource(strings = {"NotBearer 1", "Bearer StringNotInt", "Bearer 1.1", "Bearer "})
     void validateManagerAuthenticationLegacyInvalidBearerTest(String bearer) {
        Optional<User> userOptional = authenticationService.validateManagerAuthenticationLegacy(bearer);
        assertFalse(userOptional.isPresent());
    }

    @Test
     void validateManagerAuthenticationLegacyRepositoryFailsTest() {
        user.setId(1);
        user.setUsername("username");
        user.setRole("manager");

        Optional<User> userOptional = authenticationService.validateManagerAuthenticationLegacy("Bearer 1");
        assertFalse(userOptional.isPresent());
    }

    @Test
     void validateManagerAuthenticationLegacyNotManagerTest() {
        user.setId(1);
        user.setUsername("username");
        user.setRole("employee");

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        Optional<User> userOptional = authenticationService.validateManagerAuthenticationLegacy("Bearer 1");
        assertFalse(userOptional.isPresent());
        verify(userRepository).findById(anyInt());
    }

    @Test
     void getUserByIdSuccessTest() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        assertTrue(authenticationService.getUserById(1).isPresent());
        verify(userRepository).findById(anyInt());
    }

    @Test
     void getUserByIdFailTest() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertFalse(authenticationService.getUserById(1).isPresent());
        verify(userRepository).findById(anyInt());
    }

//    @Test
//     void getUserByIdExceptionTest() {
//        when(userRepository.findById(anyInt())).thenThrow(new RuntimeException());
//        assertThrows(RuntimeException.class, () -> authenticationService.getUserById(1));
//    }

    @Test
     void authenticateUserSuccessTest() {
        user.setUsername("username");
        user.setPassword("password");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        assertTrue(authenticationService.authenticateUser(user.getUsername(), user.getPassword()).isPresent());
    }

    @Test
     void authenticateUserIncorrectPasswordTest() {
        user.setUsername("username");
        user.setPassword("password");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        assertFalse(authenticationService.authenticateUser(user.getUsername(), "wrong password").isPresent());
    }

    @Test
     void authenticateUserIncorrectUsernameTest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertFalse(authenticationService.authenticateUser("username", "password").isPresent());
    }

    @Test
     void authenticateMangerSuccessTest() {
        user.setUsername("username");
        user.setPassword("password");
        user.setRole("manager");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        assertTrue(authenticationService.authenticateManager(user.getUsername(), user.getPassword()).isPresent());
    }

    @Test
     void authenticateManagerIncorrectPasswordTest() {
        user.setUsername("username");
        user.setPassword("password");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        assertFalse(authenticationService.authenticateManager(user.getUsername(), "wrong password").isPresent());
    }

    @Test
     void authenticateManagerIncorrectUsernameTest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertFalse(authenticationService.authenticateManager("username", "password").isPresent());
    }

    @Test
     void authenticateMangerNotManagerTest() {
        user.setUsername("username");
        user.setPassword("password");
        user.setRole("employee");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        assertFalse(authenticationService.authenticateManager(user.getUsername(), user.getPassword()).isPresent());
    }

}
