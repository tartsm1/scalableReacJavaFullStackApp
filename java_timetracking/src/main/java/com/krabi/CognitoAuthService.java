package com.krabi;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwk.JwkException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CognitoAuthService {
    private static final Logger logger = LoggerFactory.getLogger(CognitoAuthService.class);
    private final String userPoolId;
    private final String clientId;
    private final String region;
    private final CognitoIdentityProviderClient cognitoClient;
    private final Vertx vertx;
    private JwkProvider jwkProvider;
    private Map<Algorithm, JWTVerifier> publicKeyMap = new HashMap<>();

    public CognitoAuthService(Vertx vertx, String userPoolId, String clientId, String region) {
        this.vertx = vertx;
        this.userPoolId = userPoolId;
        this.clientId = clientId;
        this.region = region;
        this.cognitoClient = CognitoIdentityProviderClient.builder()
                .region(software.amazon.awssdk.regions.Region.of(region))
                .build();
        initializeJwkProvider();
    }

    private void initializeJwkProvider() {
        try {
            String jwksUrl = String.format("https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json", region,
                    userPoolId);
            this.jwkProvider = new JwkProviderBuilder(new URL(jwksUrl)).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JWK provider", e);
        }
    }

    @SuppressWarnings("deprecation")
    public Future<JsonObject> validateToken(String token) {
        Promise<JsonObject> promise = Promise.promise();
        // Don't remove blocking calls, this will break authentification
        vertx.executeBlocking(blockingCodeHandler -> {
            try {
                DecodedJWT jwt = JWT.decode(token);
                // Verify the token signature
                RSAPublicKey publicKey = (RSAPublicKey) jwkProvider.get(jwt.getKeyId()).getPublicKey();
                Algorithm algorithm = Algorithm.RSA256(publicKey, null);
                JWTVerifier verifier = publicKeyMap.get(algorithm);
                if (verifier == null) {
                    verifier = JWT.require(algorithm)
                            .withIssuer(String.format("https://cognito-idp.%s.amazonaws.com/%s", region, userPoolId))
                            .build();
                    publicKeyMap.put(algorithm, verifier);
                }
                DecodedJWT verifiedJwt = verifier.verify(token);

                // Accept if either aud or client_id matches clientId
                boolean audOk = false;
                if (verifiedJwt.getAudience() != null && !verifiedJwt.getAudience().isEmpty()) {
                    audOk = verifiedJwt.getAudience().contains(clientId);
                }
                boolean clientIdOk = false;
                if (verifiedJwt.getClaim("client_id") != null && !verifiedJwt.getClaim("client_id").isMissing()) {
                    clientIdOk = clientId.equals(verifiedJwt.getClaim("client_id").asString());
                }
                if (!audOk && !clientIdOk) {
                    throw new JWTVerificationException(
                            "Token audience (aud) or client_id does not match application client ID");
                }

                // Extract user information
                JsonObject userInfo = new JsonObject()
                        .put("sub", verifiedJwt.getSubject())
                        .put("email", verifiedJwt.getClaim("email").asString())
                        .put("username", verifiedJwt.getClaim("cognito:username").asString())
                        .put("groups", verifiedJwt.getClaim("cognito:groups").asList(String.class));

                logger.info("token validated, user info {}", userInfo.encodePrettily());
                blockingCodeHandler.complete(userInfo);
            } catch (JWTVerificationException | JwkException e) {
                blockingCodeHandler.fail(e);
            }
        }, result -> {
            if (result.succeeded()) {
                promise.complete((JsonObject) result.result());
            } else {
                promise.fail(result.cause());
            }
        });

        return promise.future();
    }

    // this method authenticateUser not used as authentication handled by frontend
    @SuppressWarnings("deprecation")
    public Future<JsonObject> authenticateUser(String username, String password) {
        Promise<JsonObject> promise = Promise.promise();

        vertx.executeBlocking(blockingCodeHandler -> {
            try {
                InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                        .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                        .clientId(clientId)
                        .authParameters(Map.of(
                                "USERNAME", username,
                                "PASSWORD", password))
                        .build();

                InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);

                if (authResponse.authenticationResult() != null) {
                    JsonObject result = new JsonObject()
                            .put("accessToken", authResponse.authenticationResult().accessToken())
                            .put("idToken", authResponse.authenticationResult().idToken())
                            .put("refreshToken", authResponse.authenticationResult().refreshToken());
                    blockingCodeHandler.complete(result);
                } else {
                    blockingCodeHandler.fail("Authentication failed");
                }
            } catch (Exception e) {
                blockingCodeHandler.fail(e);
            }
        }, result -> {
            if (result.succeeded()) {
                promise.complete((JsonObject) result.result());
            } else {
                promise.fail(result.cause());
            }
        });
        return promise.future();
    }

    public void close() {
        if (cognitoClient != null) {
            cognitoClient.close();
        }
    }
}