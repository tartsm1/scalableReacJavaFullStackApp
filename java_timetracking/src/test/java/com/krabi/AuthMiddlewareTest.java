package com.krabi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@ExtendWith(MockitoExtension.class)
class AuthMiddlewareTest {

    @Mock
    private CognitoAuthService authService;
    @Mock
    private RoutingContext routingContext;
    @Mock
    private HttpServerRequest request;
    @Mock
    private HttpServerResponse response;

    private AuthMiddleware authMiddleware;

    @BeforeEach
    public void setUp() {
        authMiddleware = new AuthMiddleware(authService);
        lenient().when(routingContext.request()).thenReturn(request);
        lenient().when(routingContext.response()).thenReturn(response);
        lenient().when(response.setStatusCode(anyInt())).thenReturn(response);
        lenient().when(response.putHeader(anyString(), anyString())).thenReturn(response);
        lenient().when(routingContext.put(anyString(), any())).thenReturn(routingContext);
    }

    @Test
    void authenticate_ShouldSucceed_WhenTokenIsValid() {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        JsonObject userInfo = new JsonObject().put("username", "testuser");
        when(authService.validateToken("valid-token")).thenReturn(Future.succeededFuture(userInfo));

        Handler<RoutingContext> handler = authMiddleware.authenticate();
        handler.handle(routingContext);

        verify(routingContext).put("user", userInfo);
        verify(routingContext).next();
    }

    @Test
    void authenticate_ShouldFail_WhenNoAuthHeader() {
        when(request.getHeader("Authorization")).thenReturn(null);

        Handler<RoutingContext> handler = authMiddleware.authenticate();
        handler.handle(routingContext);

        verify(response).setStatusCode(401);
        verify(response).end(anyString());
        verify(routingContext, never()).next();
    }

    @Test
    void authenticate_ShouldFail_WhenInvalidAuthHeaderFormat() {
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat");

        Handler<RoutingContext> handler = authMiddleware.authenticate();
        handler.handle(routingContext);

        verify(response).setStatusCode(401);
        verify(response).end(anyString());
        verify(routingContext, never()).next();
    }

    @Test
    void authenticate_ShouldFail_WhenTokenIsInvalid() {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(authService.validateToken("invalid-token")).thenReturn(Future.failedFuture("Invalid token"));

        Handler<RoutingContext> handler = authMiddleware.authenticate();
        handler.handle(routingContext);

        verify(response).setStatusCode(401);
        verify(response).end(anyString());
        verify(routingContext, never()).next();
    }

    @Test
    void authenticate_ShouldSetDevUser_WhenInDevMode() {
        AuthMiddleware devMiddleware = new AuthMiddleware(); // No authService
        
        Handler<RoutingContext> handler = devMiddleware.authenticate();
        handler.handle(routingContext);

        verify(routingContext).put(eq("user"), any(JsonObject.class));
        verify(routingContext).next();
    }

    @Test
    void requireRole_ShouldSucceed_WhenUserHasRole() {
        JsonObject user = new JsonObject().put("groups", new JsonArray().add("admin"));
        when(routingContext.get("user")).thenReturn(user);

        Handler<RoutingContext> handler = authMiddleware.requireRole("admin");
        handler.handle(routingContext);

        verify(routingContext).next();
    }

    @Test
    void requireRole_ShouldFail_WhenUserDoesNotHaveRole() {
        JsonObject user = new JsonObject().put("groups", new JsonArray().add("user"));
        when(routingContext.get("user")).thenReturn(user);

        Handler<RoutingContext> handler = authMiddleware.requireRole("admin");
        handler.handle(routingContext);

        verify(response).setStatusCode(403);
        verify(response).end(anyString());
        verify(routingContext, never()).next();
    }

    @Test
    void requireRole_ShouldFail_WhenUserIsNotAuthenticated() {
        when(routingContext.get("user")).thenReturn(null);

        Handler<RoutingContext> handler = authMiddleware.requireRole("admin");
        handler.handle(routingContext);

        verify(response).setStatusCode(401);
        verify(response).end(anyString());
        verify(routingContext, never()).next();
    }
}
