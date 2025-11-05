package com.krabi;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthMiddleware {
    private static final Logger logger = LoggerFactory.getLogger(AuthMiddleware.class);
    private final CognitoAuthService authService;

    public AuthMiddleware(CognitoAuthService authService) {
        this.authService = authService;
    }

    public AuthMiddleware() {
        this.authService = null; // In development environment
    }

    public Handler<RoutingContext> authenticate() {
        if (authService == null) { // In development environment
            logger.info("devuser: devuser");
            return ctx -> ctx.put("user", new JsonObject()
                    .put("username", "devuser"))
                    .next();
        }

        return ctx -> {
            String authHeader = ctx.request().getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                ctx.response()
                        .setStatusCode(401)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject()
                                .put("error", "Missing or invalid Authorization header")
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
                                        .put("error", "Invalid token")
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
                                .put("error", "Authentication required")
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
                                .put("error", "Insufficient permissions")
                                .put("required_role", role)
                                .encode());
            }
        };
    }
}