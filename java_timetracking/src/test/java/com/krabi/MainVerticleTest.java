package com.krabi;

import java.net.ServerSocket;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@ExtendWith(VertxExtension.class)
class MainVerticleTest {

    private int port;

    /**
     * A testable subclass that overrides getEnv to provide test configuration
     * without needing reflection or module system hacks.
     */
    static class TestableMainVerticle extends MainVerticle {
        private final Map<String, String> envOverrides;

        TestableMainVerticle(Map<String, String> envOverrides) {
            this.envOverrides = envOverrides;
        }

        @Override
        protected String getEnv(String name) {
            return envOverrides.getOrDefault(name, null);
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
    }

    private TestableMainVerticle createDevVerticle() {
        return new TestableMainVerticle(Map.of(
                "dev", "true",
                "port", String.valueOf(port),
                "host", "localhost",
                "DYNAMODB_ENDPOINT", "http://localhost:8000",
                "AWS_REGION", "eu-north-1"
        ));
    }

    @Test
    void testStartInDevMode_ShouldStartServer(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(createDevVerticle())
                .onSuccess(id -> {
                    assertNotNull(id);
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    void testTestEndpoint_ShouldReturnOk(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(createDevVerticle())
                .onSuccess(id -> {
                    WebClient client = WebClient.create(vertx);
                    client.get(port, "localhost", "/api/test")
                            .send()
                            .onSuccess(response -> testContext.verify(() -> {
                                assertEquals(200, response.statusCode());
                                assertEquals("Ok", response.bodyAsString());
                                testContext.completeNow();
                            }))
                            .onFailure(testContext::failNow);
                })
                .onFailure(testContext::failNow);
    }

    @Test
    void testAuthTestEndpoint_InDevMode_ShouldReturnDevUser(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(createDevVerticle())
                .onSuccess(id -> {
                    WebClient client = WebClient.create(vertx);
                    client.get(port, "localhost", "/api/authtest")
                            .send()
                            .onSuccess(response -> testContext.verify(() -> {
                                assertEquals(200, response.statusCode());
                                assertTrue(response.bodyAsString().contains("devuser"));
                                testContext.completeNow();
                            }))
                            .onFailure(testContext::failNow);
                })
                .onFailure(testContext::failNow);
    }

    @Test
    void testGetTasks_InDevMode_ShouldReturnResponse(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(createDevVerticle())
                .onSuccess(id -> {
                    WebClient client = WebClient.create(vertx);
                    client.get(port, "localhost", "/api/tasks")
                            .send()
                            .onSuccess(response -> testContext.verify(() -> {
                                assertNotNull(response);
                                testContext.completeNow();
                            }))
                            .onFailure(testContext::failNow);
                })
                .onFailure(testContext::failNow);
    }

    @Test
    void testVerticleIsDev_WhenDevEnvIsTrue(Vertx vertx, VertxTestContext testContext) {
        TestableMainVerticle verticle = createDevVerticle();
        vertx.deployVerticle(verticle)
                .onSuccess(id -> testContext.verify(() -> {
                    assertTrue(verticle.isDev);
                    testContext.completeNow();
                }))
                .onFailure(testContext::failNow);
    }

    @Test
    void testPostTask_InDevMode_ShouldAcceptRequest(Vertx vertx, VertxTestContext testContext) {
        vertx.deployVerticle(createDevVerticle())
                .onSuccess(id -> {
                    WebClient client = WebClient.create(vertx);
                    JsonObject taskJson = new JsonObject()
                            .put("date", "2023-10-27")
                            .put("project", "Test Project")
                            .put("hours", 8)
                            .put("task", "Testing");

                    client.post(port, "localhost", "/api/tasks")
                            .sendJsonObject(taskJson)
                            .onSuccess(response -> testContext.verify(() -> {
                                assertNotNull(response);
                                testContext.completeNow();
                            }))
                            .onFailure(testContext::failNow);
                })
                .onFailure(testContext::failNow);
    }
}
