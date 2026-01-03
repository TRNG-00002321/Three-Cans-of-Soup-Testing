package com.revature.Unit.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Tag;
import org.mockito.junit.jupiter.MockitoExtension;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.revature.repository.User;
import com.revature.repository.UserRepository;
import com.revature.service.AuthenticationService;

@Epic("Manager App")
@Feature("Manager Authentication")
@Tag("Unit")
@Tag("Sprint-2")
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
    @Story("Manager Login")
    @Description("Verify valid JWT token creation")
    @Severity(SeverityLevel.CRITICAL)
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
    @Story("Manager Login")
    @Description("Verify successful JWT token validation")
    @Severity(SeverityLevel.CRITICAL)
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
    @Story("Manager Login")
    @Description("Verify validation fails for empty/blank tokens")
    @Severity(SeverityLevel.NORMAL)
    void validateJwtTokenNoTokenTest(String strings) {
        Optional<User> userOptional = authenticationService.validateJwtToken(strings);
        assertFalse(userOptional.isPresent());
    }

    @Test
    @Story("Manager Login")
    @Description("Verify validation fails for invalid token strings")
    @Severity(SeverityLevel.NORMAL)
    void validateJwtTokenInvalidTokenTest() {
        Optional<User> userOptional = authenticationService.validateJwtToken("fake token");
        assertFalse(userOptional.isPresent());
    }

    @Test
    @Story("Manager Login")
    @Description("Verify validation fails when user ID is invalid")
    @Severity(SeverityLevel.NORMAL)
    void validateJwtTokenInvalidIDTest() {
        when(userRepository.findById(anyInt())).thenThrow(NumberFormatException.class);

        String token = authenticationService.createJwtToken(user);
        Optional<User> userOptional = authenticationService.validateJwtToken(token);
        assertFalse(userOptional.isPresent());
    }

    @Test
    @Story("Manager Login")
    @Description("Verify authentication validation with valid bearer token")
    @Severity(SeverityLevel.CRITICAL)
    void validateAuthenticationSuccessTest() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        Optional<User> newUser = authenticationService.validateAuthentication("Bearer 1");
        assertTrue(newUser.isPresent());
    }

    @ParameterizedTest
    //@NullSource
    @ValueSource(strings = {"NotBearer 1", "Bearer StringNotInt", "Bearer 1.1", "Bearer "})
    @Story("Manager Login")
    @Description("Verify authentication validation fails with invalid bearer format")
    @Severity(SeverityLevel.NORMAL)
    void validateAuthenticationInvalidBearerTest(String strings) {
        Optional<User> newUser = authenticationService.validateAuthentication(strings);
        assertFalse(newUser.isPresent());
    }

    @Test
    @Story("Manager Login")
    @Description("Verify manager role check returns true for manager")
    @Severity(SeverityLevel.NORMAL)
    void isManagerTrueTest() {
        user.setRole("manager");
        assertTrue(authenticationService.isManager(user));
    }

    @Test
    @Story("Manager Login")
    @Description("Verify manager role check returns false for non-manager")
    @Severity(SeverityLevel.NORMAL)
    void isManagerFalseTest() {
        user.setRole("employee");
        assertFalse(authenticationService.isManager(user));
        assertFalse(authenticationService.isManager(null));
    }

    @Test
    @Story("Manager Login")
    @Description("Verify successful manager authentication")
    @Severity(SeverityLevel.CRITICAL)
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
    @Story("Manager Login")
    @Description("Verify manager authentication fails with invalid token")
    @Severity(SeverityLevel.NORMAL)
    void validateManagerAuthenticationInvalidTokenTest() {
        Optional<User> userOptional = authenticationService.validateManagerAuthentication("token");
        assertFalse(userOptional.isPresent());

        // userRepo fails
        user.setId(1);
        user.setUsername("username");
        user.setRole("manager");

        String token = authenticationService.createJwtToken(user);
        userOptional = authenticationService.validateManagerAuthentication(token);
        assertFalse(userOptional.isPresent());
    }

    @Test
    @Story("Manager Login")
    @Description("Verify manager authentication fails for non-manager user")
    @Severity(SeverityLevel.NORMAL)
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
    @Story("Manager Login")
    @Description("Verify successful legacy manager authentication")
    @Severity(SeverityLevel.CRITICAL)
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
    @Story("Manager Login")
    @Description("Verify legacy authentication fails with invalid bearer format")
    @Severity(SeverityLevel.NORMAL)
    void validateManagerAuthenticationLegacyInvalidBearerTest(String bearer) {
        Optional<User> userOptional = authenticationService.validateManagerAuthenticationLegacy(bearer);
        assertFalse(userOptional.isPresent());
    }

    @Test
    @Story("Manager Login")
    @Description("Verify legacy authentication fails when repository fails")
    @Severity(SeverityLevel.CRITICAL)
    void validateManagerAuthenticationLegacyRepositoryFailsTest() {
        user.setId(1);
        user.setUsername("username");
        user.setRole("manager");

        Optional<User> userOptional = authenticationService.validateManagerAuthenticationLegacy("Bearer 1");
        assertFalse(userOptional.isPresent());
    }

    @Test
    @Story("Manager Login")
    @Description("Verify legacy authentication fails for non-manager")
    @Severity(SeverityLevel.NORMAL)
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
    @Story("Manager Login")
    @Description("Verify retrieval of user by ID")
    @Severity(SeverityLevel.NORMAL)
    void getUserByIdSuccessTest() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        assertTrue(authenticationService.getUserById(1).isPresent());
        verify(userRepository).findById(anyInt());
    }

    @Test
    @Story("Manager Login")
    @Description("Verify nothing returned when user ID not found")
    @Severity(SeverityLevel.NORMAL)
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
    @Story("Manager Login")
    @Description("Verify successful user authentication")
    @Severity(SeverityLevel.CRITICAL)
    void authenticateUserSuccessTest() {
        user.setUsername("username");
        user.setPassword("password");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        assertTrue(authenticationService.authenticateUser(user.getUsername(), user.getPassword()).isPresent());
    }

    @Test
    @Story("Manager Login")
    @Description("Verify authentication fails with incorrect password")
    @Severity(SeverityLevel.NORMAL)
    void authenticateUserIncorrectPasswordTest() {
        user.setUsername("username");
        user.setPassword("password");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        assertFalse(authenticationService.authenticateUser(user.getUsername(), "wrong password").isPresent());
    }

    @Test
    @Story("Manager Login")
    @Description("Verify authentication fails with incorrect username")
    @Severity(SeverityLevel.NORMAL)
    void authenticateUserIncorrectUsernameTest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertFalse(authenticationService.authenticateUser("username", "password").isPresent());
    }

    @Test
    @Story("Manager Login")
    @Description("Verify successful manager authentication (direct)")
    @Severity(SeverityLevel.CRITICAL)
    void authenticateMangerSuccessTest() {
        user.setUsername("username");
        user.setPassword("password");
        user.setRole("manager");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        assertTrue(authenticationService.authenticateManager(user.getUsername(), user.getPassword()).isPresent());
    }

    @Test
    @Story("Manager Login")
    @Description("Verify manager authentication fails with incorrect password")
    @Severity(SeverityLevel.NORMAL)
    void authenticateManagerIncorrectPasswordTest() {
        user.setUsername("username");
        user.setPassword("password");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        assertFalse(authenticationService.authenticateManager(user.getUsername(), "wrong password").isPresent());
    }

    @Test
    @Story("Manager Login")
    @Description("Verify manager authentication fails with incorrect username")
    @Severity(SeverityLevel.NORMAL)
    void authenticateManagerIncorrectUsernameTest() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertFalse(authenticationService.authenticateManager("username", "password").isPresent());
    }

    @Test
    @Story("Manager Login")
    @Description("Verify manager authentication fails for non-manager role")
    @Severity(SeverityLevel.NORMAL)
    void authenticateMangerNotManagerTest() {
        user.setUsername("username");
        user.setPassword("password");
        user.setRole("employee");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        assertFalse(authenticationService.authenticateManager(user.getUsername(), user.getPassword()).isPresent());
    }

}
