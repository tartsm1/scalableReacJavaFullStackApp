package com.krabi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

public class MainVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
    private static final String COGNITO_USER_POOL_ID = "COGNITO_USER_POOL_ID";
    private static final String COGNITO_CLIENT_ID = "COGNITO_CLIENT_ID";
    private static final String AWS_REGION = "AWS_REGION";
    private static final String PORT = "port";
    private static final String HOST = "host";
    private static final String DEV = "dev";
    private static final String ALLOWED_ORIGINS = "ALLOWED_ORIGINS";
    private static final String PLEASE_SET_AWS_ENVIRONMENT_VARIABLES = "Please set COGNITO_USER_POOL_ID, COGNITO_CLIENT_ID, and AWS_REGION environment variables.";
    private static final String HTTP_SERVER_STARTED_ON_PORT = "HTTP server started on port ";
    private static final String HTTP_SERVER_FAILED_TO_START = "HTTP server failed to start";
    private static final String USER = "user";
    private static final String THREADS_COUNT = "threadsCount";
    private boolean isDev = false;
    private CognitoAuthService authService;

    public static void main(String[] args) {

        String threadsCount = System.getenv(THREADS_COUNT);
        int threads = (threadsCount != null) ? Integer.parseInt(threadsCount) : 1;

        // Configure Vert.x with configured event loop threads
        VertxOptions options = new VertxOptions().setEventLoopPoolSize(threads);
        Vertx vertx = Vertx.vertx(options);

        // Deploy configured instances of the verticle
        DeploymentOptions deploymentOptions = new DeploymentOptions().setInstances(threads);
        vertx.deployVerticle(MainVerticle.class.getName(), deploymentOptions)
                .onSuccess(deploymentId -> logger.info("Deployed {} instances of MainVerticle with deployment ID: {}",
                        String.valueOf(threads), deploymentId))
                .onFailure(cause -> logger.error("Failed to deploy MainVerticle", cause));
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        Router router = Router.router(vertx);
        TaskService taskService = new TaskService(DynamoDBClientProvider.getClient());

        // These values should be configured via environment variables
        // Initialize Cognito authentication
        String userPoolId = getEnv(COGNITO_USER_POOL_ID);
        String clientId = getEnv(COGNITO_CLIENT_ID);
        String region = getEnv(AWS_REGION);

        String port = getEnv(PORT);
        String host = getEnv(HOST);

        // Check for dev mode
        isDev = "true".equalsIgnoreCase(getEnv(DEV));
        logger.info("isDev: {}", isDev);
        logger.debug("userPoolId: {}", userPoolId);
        logger.debug("clientId: {}", clientId);
        logger.debug("region: {}", region);
        logger.info("port: {}", port);
        logger.info("host: {}", host);

        // Initialize Cognito authentication only if not in dev mode
        final AuthMiddleware authMiddleware;
        if (!isDev) {
            if (userPoolId == null || clientId == null || region == null) {
                logger.warn(PLEASE_SET_AWS_ENVIRONMENT_VARIABLES);
                startPromise.fail(PLEASE_SET_AWS_ENVIRONMENT_VARIABLES);
                return;
            }
            authService = new CognitoAuthService(vertx, userPoolId, clientId, region);
            authMiddleware = new AuthMiddleware(authService);
        } else {
            authMiddleware = new AuthMiddleware();
        }

        // CORS configuration
        String allowedOrigins = getEnv(ALLOWED_ORIGINS);
        if (allowedOrigins != null) {
            router.route().handler(CorsHandler.create()
                    .addOrigin(allowedOrigins)
                    .allowedMethod(HttpMethod.GET)
                    .allowedMethod(HttpMethod.POST)
                    .allowedMethod(HttpMethod.PUT)
                    .allowedMethod(HttpMethod.DELETE)
                    .allowedMethod(HttpMethod.OPTIONS)
                    .allowedHeader("Authorization")
                    .allowedHeader("Content-Type"));
        }
        // Create API router with /api prefix
        Router apiRouter = Router.router(vertx);

        // Add BodyHandler to parse request bodies
        apiRouter.route().handler(BodyHandler.create());

        // Global error handler
        apiRouter.route().failureHandler(ctx -> {
            Throwable failure = ctx.failure();
            int code = ctx.statusCode() > 0 ? ctx.statusCode() : 500;
            String message = failure != null ? failure.getMessage() : "Internal server error";
            logger.error("Request failed: {} {}, status: {}", ctx.request().method(), ctx.request().path(), code,
                    failure);
            ctx.response()
                    .setStatusCode(code)
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("error", message).encode());
        });

        // Mount API router under /api
        router.route("/api/*").subRouter(apiRouter);

        // Protected routes - require authentication
        apiRouter.get("/tasks").handler(authMiddleware.authenticate()).handler(ctx -> {
            String username = getUserNameFromCtx(ctx);
            vertx.executeBlocking(() -> taskService.listTasks(username))
                    .onSuccess(tasks -> ctx.response()
                            .putHeader("content-type", "application/json")
                            .end(Json.encode(tasks)))
                    .onFailure(ctx::fail);
        });

        apiRouter.get("/tasks/:id").handler(authMiddleware.authenticate()).handler(ctx -> {
            long id;
            try {
                id = Long.parseLong(ctx.pathParam("id"));
            } catch (NumberFormatException e) {
                ctx.response().setStatusCode(400)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", "Invalid task ID").encode());
                return;
            }
            String username = getUserNameFromCtx(ctx);
            vertx.executeBlocking(() -> taskService.getTask(id))
                    .onSuccess(task -> {
                        if (task == null) {
                            ctx.response().setStatusCode(404).end();
                        } else if (username != null && !username.equals(task.username())) {
                            ctx.response().setStatusCode(403)
                                    .putHeader("content-type", "application/json")
                                    .end(new JsonObject().put("error", "Access denied").encode());
                        } else {
                            ctx.response().putHeader("content-type", "application/json").end(Json.encode(task));
                        }
                    })
                    .onFailure(ctx::fail);
        });

        apiRouter.post("/tasks").handler(authMiddleware.authenticate()).handler(ctx -> {
            Task task;
            try {
                task = ctx.body().asPojo(Task.class);
            } catch (Exception e) {
                ctx.response().setStatusCode(400)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", "Invalid request body").encode());
                return;
            }
            Task taskWithUser = task.withUsername(getUserNameFromCtx(ctx));
            vertx.executeBlocking(() -> taskService.createTask(taskWithUser))
                    .onSuccess(created -> ctx.response()
                            .setStatusCode(201)
                            .putHeader("content-type", "application/json")
                            .end(Json.encode(created)))
                    .onFailure(ctx::fail);
        });

        apiRouter.put("/tasks/:id").handler(authMiddleware.authenticate()).handler(ctx -> {
            long id;
            try {
                id = Long.parseLong(ctx.pathParam("id"));
            } catch (NumberFormatException e) {
                ctx.response().setStatusCode(400)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", "Invalid task ID").encode());
                return;
            }
            Task task;
            try {
                task = ctx.body().asPojo(Task.class);
            } catch (Exception e) {
                ctx.response().setStatusCode(400)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", "Invalid request body").encode());
                return;
            }
            String username = getUserNameFromCtx(ctx);
            // Verify ownership before updating
            vertx.executeBlocking(() -> taskService.getTask(id))
                    .onSuccess(existing -> {
                        if (existing != null && username != null && !username.equals(existing.username())) {
                            ctx.response().setStatusCode(403)
                                    .putHeader("content-type", "application/json")
                                    .end(new JsonObject().put("error", "Access denied").encode());
                            return;
                        }
                        Task taskToUpdate = task.withIdAndUsername(id, username);
                        vertx.executeBlocking(() -> taskService.updateTask(taskToUpdate))
                                .onSuccess(v -> ctx.response().setStatusCode(204).end())
                                .onFailure(ctx::fail);
                    })
                    .onFailure(ctx::fail);
        });

        apiRouter.delete("/tasks/:id").handler(authMiddleware.authenticate()).handler(ctx -> {
            long id;
            try {
                id = Long.parseLong(ctx.pathParam("id"));
            } catch (NumberFormatException e) {
                ctx.response().setStatusCode(400)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", "Invalid task ID").encode());
                return;
            }
            String username = getUserNameFromCtx(ctx);
            // Verify ownership before deleting
            vertx.executeBlocking(() -> taskService.getTask(id))
                    .onSuccess(existing -> {
                        if (existing == null) {
                            ctx.response().setStatusCode(404).end();
                            return;
                        }
                        if (username != null && !username.equals(existing.username())) {
                            ctx.response().setStatusCode(403)
                                    .putHeader("content-type", "application/json")
                                    .end(new JsonObject().put("error", "Access denied").encode());
                            return;
                        }
                        vertx.executeBlocking(() -> {
                            taskService.deleteTask(id);
                            return null;
                        })
                                .onSuccess(v -> ctx.response().setStatusCode(204).end())
                                .onFailure(ctx::fail);
                    })
                    .onFailure(ctx::fail);
        });

        apiRouter.get("/authtest").handler(authMiddleware.authenticate()).handler(ctx -> {
            ctx.response().end("User authenticated - " + getUserNameFromCtx(ctx));
        });
        apiRouter.get("/test").handler(ctx -> {
            ctx.response().end("Ok");
        });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(Integer.parseInt(port), host)
                .onSuccess(http -> {
                    startPromise.complete();
                    logger.info(HTTP_SERVER_STARTED_ON_PORT + "{}", port);
                })
                .onFailure(cause -> {
                    logger.error(HTTP_SERVER_FAILED_TO_START, cause);
                    startPromise.fail(cause);
                });
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        if (authService != null) {
            authService.close();
        }
        stopPromise.complete();
    }

    protected String getEnv(String name) {
        return System.getenv(name);
    }

    boolean isDev() {
        return isDev;
    }

    private String getUserNameFromCtx(RoutingContext ctx) {
        // Set username from authenticated user
        String username = null;
        if (ctx.get(USER) != null && ctx.get(USER) instanceof io.vertx.core.json.JsonObject) {
            username = ((io.vertx.core.json.JsonObject) ctx.get(USER)).getString(AuthMiddleware.USERNAME);
        }
        return username;
    }
}
