package com.krabi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class AuthMiddleware {
    private static final Logger logger = LoggerFactory.getLogger(AuthMiddleware.class);
    private static final String DEVUSER = "devuser";
    static final String USERNAME = "username";
    protected static final String INVALID_TOKEN = "Invalid token";
    protected static final String MISSING_OR_INVALID_AUTHORIZATION_HEADER = "Missing or invalid Authorization header";
    protected static final String AUTHENTICATION_REQUIRED = "Authentication required";
    protected static final String INSUFFICIENT_PERMISSIONS = "Insufficient permissions";
    private final CognitoAuthService authService;

    public AuthMiddleware(CognitoAuthService authService) {
        this.authService = authService;
    }

    public AuthMiddleware() {
        // In development environment
        this.authService = null;
    }

    public Handler<RoutingContext> authenticate() {
        if (authService == null) { // In development environment
            logger.info("In development - devuser");
            return ctx -> ctx.put("user", new JsonObject()
                    .put(USERNAME, DEVUSER))
                    .next();
        }

        return ctx -> {
            String authHeader = ctx.request().getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ctx.response()
                        .setStatusCode(401)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject()
                                .put("error", MISSING_OR_INVALID_AUTHORIZATION_HEADER)
                                .encode());
                return;
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix

            authService.validateToken(token)
                    .onSuccess(userInfo -> {
                        ctx.put("user", userInfo);
                        ctx.next();
                    })
                    .onFailure(err -> {
                        ctx.response()
                                .setStatusCode(401)
                                .putHeader("content-type", "application/json")
                                .end(new JsonObject()
                                        .put("error", INVALID_TOKEN)
                                        .put("message", err.getMessage())
                                        .encode());
                    });
        };
    }

    public Handler<RoutingContext> requireRole(String role) {
        return ctx -> {
            JsonObject user = ctx.get("user");
            if (user == null) {
                ctx.response()
                        .setStatusCode(401)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject()
                                .put("error", AUTHENTICATION_REQUIRED)
                                .encode());
                return;
            }

            // Check if user has the required role
            if (user.getJsonArray("groups") != null &&
                    user.getJsonArray("groups").contains(role)) {
                ctx.next();
            } else {
                ctx.response()
                        .setStatusCode(403)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject()
                                .put("error", INSUFFICIENT_PERMISSIONS)
                                .put("required_role", role)
                                .encode());
            }
        };
    }
}