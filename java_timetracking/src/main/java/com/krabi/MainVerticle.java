package com.krabi;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.json.Json;

import java.util.List;

import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(MainVerticle.class);
    private static final String COGNITO_USER_POOL_ID = "COGNITO_USER_POOL_ID";
    private static final String COGNITO_CLIENT_ID = "COGNITO_CLIENT_ID";
    private static final String AWS_REGION = "AWS_REGION";
    private static final String PORT = "port";
    private static final String HOST = "host";
    private static final String DEV = "dev";
    private static final String PLEASE_SET_AWS_ENVIRONMENT_VARIABLES = "Please set COGNITO_USER_POOL_ID, COGNITO_CLIENT_ID, and AWS_REGION environment variables.";
    private static final String HTTP_SERVER_STARTED_ON_PORT = "HTTP server started on port ";
    private static final String HTTP_SERVER_FAILED_TO_START = "HTTP server failed to start";
    private static final String USER = "user";
    boolean isDev = false;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle());
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        Router router = Router.router(vertx);
        TaskService taskService = new TaskService(DynamoDBClientProvider.getClient());

        // These values should be configured via environment variables
        // Initialize Cognito authentication
        String userPoolId = System.getenv(COGNITO_USER_POOL_ID);
        String clientId = System.getenv(COGNITO_CLIENT_ID);
        String region = System.getenv(AWS_REGION);

        String port = System.getenv(PORT);
        String host = System.getenv(HOST);

        // Check for dev mode
        isDev = "true".equalsIgnoreCase(System.getenv(DEV));
        logger.info("isDev: {}", isDev);
        logger.info("userPoolId: {}", userPoolId);
        logger.info("clientId: {}", clientId);
        logger.info("region: {}", region);
        logger.info("port: {}", port);
        logger.info("host: {}", host);

        // Initialize Cognito authentication only if not in dev mode
        final CognitoAuthService authService;
        final AuthMiddleware authMiddleware;
        if (!isDev) {
            if (userPoolId == null || clientId == null || region == null) {
                logger.warn(PLEASE_SET_AWS_ENVIRONMENT_VARIABLES);
                System.exit(-1);
            }
            authService = new CognitoAuthService(vertx, userPoolId, clientId, region);
            authMiddleware = new AuthMiddleware(authService);
        } else {
            authMiddleware = new AuthMiddleware();
        }

        // Create API router with /api prefix
        Router apiRouter = Router.router(vertx);

        // Add BodyHandler to parse request bodies
        apiRouter.route().handler(BodyHandler.create());

        // Mount API router under /api
        router.route("/api/*").subRouter(apiRouter);

        // Protected routes - require authentication in prod
        apiRouter.get("/tasks").handler(authMiddleware.authenticate()).handler(ctx -> {
            List<Task> tasks = taskService.listTasks(getUserNameFromCtx(ctx));
            ctx.response().putHeader("content-type", "application/json").end(Json.encode(tasks));
        });
        apiRouter.get("/tasks/:id").handler(authMiddleware.authenticate()).handler(ctx -> {
            long id = Long.parseLong(ctx.pathParam("id"));
            Task task = taskService.getTask(id);
            if (task == null) {
                ctx.response().setStatusCode(404).end();
            } else {
                ctx.response().putHeader("content-type", "application/json").end(Json.encode(task));
            }
        });
        apiRouter.post("/tasks").handler(authMiddleware.authenticate()).handler(ctx -> {
            Task task = ctx.body().asPojo(Task.class);
            task.setUsername(getUserNameFromCtx(ctx));
            taskService.createTask(task);
            ctx.response().setStatusCode(201).end();
        });
        apiRouter.put("/tasks/:id").handler(authMiddleware.authenticate()).handler(ctx -> {
            long id = Long.parseLong(ctx.pathParam("id"));
            Task task = ctx.body().asPojo(Task.class);
            task.setId(id);
            task.setUsername(getUserNameFromCtx(ctx));
            taskService.updateTask(task);
            ctx.response().setStatusCode(204).end();
        });
        apiRouter.delete("/tasks/:id").handler(authMiddleware.authenticate()).handler(ctx -> {
            long id = Long.parseLong(ctx.pathParam("id"));
            taskService.deleteTask(id);
            ctx.response().setStatusCode(204).end();
        });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(Integer.parseInt(port), host, http -> {
                    if (http.succeeded()) {
                        startPromise.complete();
                        logger.info(HTTP_SERVER_STARTED_ON_PORT + "{}", port);
                    } else {
                        logger.error(HTTP_SERVER_FAILED_TO_START, http.cause());
                        startPromise.fail(http.cause());
                    }
                });
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