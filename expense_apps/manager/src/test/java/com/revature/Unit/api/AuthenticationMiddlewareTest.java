package com.revature.Unit.api;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.api.AuthenticationMiddleware;
import com.revature.repository.User;
import com.revature.service.AuthenticationService;

import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;

@ExtendWith(MockitoExtension.class)
class AuthenticationMiddlewareTest {

    @Mock
    AuthenticationService authenticationService;

    @Mock
    User user;

    @Mock
    Context ctx;

    AuthenticationMiddleware authenticationMiddleware;

    @BeforeEach
    void setUp() {
        authenticationMiddleware = new AuthenticationMiddleware(authenticationService);
    }

    @Test
    void validateManagerSuccessTest() {
        String jwtToken = "jwtToken";
        when(ctx.cookie("jwt")).thenReturn(jwtToken);
        when(authenticationService.validateManagerAuthentication(jwtToken)).thenReturn(Optional.of(user));
        Handler handler = authenticationMiddleware.validateManager();

        assertDoesNotThrow(() -> handler.handle(ctx));

        verify(ctx).cookie("jwt");
        verify(authenticationService).validateManagerAuthentication(jwtToken);
        verify(authenticationService, never()).validateJwtToken(any());
        // validate token should only work if managerOpt is empty
        // so it should not be called in this test
        verify(ctx).attribute("manager", user);
    }

    @Test
    void validateManagerUnauthorizedTest() {
        when(ctx.cookie("jwt")).thenReturn(null);
        when(authenticationService.validateManagerAuthentication(null))
                .thenReturn(Optional.empty());
        when(authenticationService.validateJwtToken(null))
                .thenReturn(Optional.empty());

        Handler handler = authenticationMiddleware.validateManager();

        assertThrows(UnauthorizedResponse.class, () -> handler.handle(ctx));
        verify(ctx, never()).attribute(eq("manager"), any());
    }

    @Test
    void validateManagerNotManagerTest() {
        String jwt = "user";

        when(ctx.cookie("jwt")).thenReturn(jwt);
        when(authenticationService.validateManagerAuthentication(jwt))
                .thenReturn(Optional.empty());
        when(authenticationService.validateJwtToken(jwt))
                .thenReturn(Optional.of(user));

        Handler handler = authenticationMiddleware.validateManager();

        assertThrows(ForbiddenResponse.class, () -> handler.handle(ctx));
        verify(ctx, never()).attribute(eq("manager"), any());
    }

    @Test
    void getAuthenticatedManagerTest() {
        when(ctx.attribute("manager")).thenReturn(user);

        User newUser = AuthenticationMiddleware.getAuthenticatedManager(ctx);

        verify(ctx).attribute("manager");
        assertEquals(newUser, user);
    }
}
