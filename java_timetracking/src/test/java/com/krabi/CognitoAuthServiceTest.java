package com.krabi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class CognitoAuthServiceTest {

    @Test
    void testConstructor_ShouldInitializeSuccessfully(Vertx vertx) {
        // Constructor initializes JwkProvider with a URL that may or may not be reachable,
        // but the URL itself is valid, so construction should succeed.
        CognitoAuthService service = new CognitoAuthService(vertx, "us-east-1_TestPool", "testClientId", "us-east-1");
        assertNotNull(service);
        service.close();
    }

    @Test
    void testClose_ShouldNotThrow(Vertx vertx) {
        CognitoAuthService service = new CognitoAuthService(vertx, "us-east-1_TestPool", "testClientId", "us-east-1");
        assertDoesNotThrow(service::close);
    }

    @Test
    void testClose_MultipleTimes_ShouldNotThrow(Vertx vertx) {
        CognitoAuthService service = new CognitoAuthService(vertx, "us-east-1_TestPool", "testClientId", "us-east-1");
        assertDoesNotThrow(() -> {
            service.close();
            service.close(); // Should handle double-close gracefully
        });
    }

    @Test
    void testValidateToken_ShouldFailWithGarbageToken(Vertx vertx, VertxTestContext testContext) {
        CognitoAuthService service = new CognitoAuthService(vertx, "us-east-1_TestPool", "testClientId", "us-east-1");

        service.validateToken("not-a-valid-jwt-token")
                .onSuccess(result -> testContext.failNow("Should have failed with invalid token"))
                .onFailure(err -> {
                    testContext.verify(() -> assertNotNull(err.getMessage()));
                    testContext.completeNow();
                });
    }

    @Test
    void testAuthenticateUser_ShouldFailWithInvalidCredentials(Vertx vertx, VertxTestContext testContext) {
        CognitoAuthService service = new CognitoAuthService(vertx, "us-east-1_TestPool", "testClientId", "us-east-1");

        service.authenticateUser("fakeuser", "fakepassword")
                .onSuccess(result -> testContext.failNow("Should have failed with invalid credentials"))
                .onFailure(err -> {
                    testContext.verify(() -> assertNotNull(err.getMessage()));
                    testContext.completeNow();
                });
    }
}
