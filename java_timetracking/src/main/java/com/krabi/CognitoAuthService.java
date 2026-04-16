package com.krabi;

import java.net.MalformedURLException;
import java.net.URI;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class CognitoAuthService {

    private static final Logger logger = LoggerFactory.getLogger(CognitoAuthService.class);
    private final String userPoolId;
    private final String clientId;
    private final String region;
    private final Vertx vertx;
    private JwkProvider jwkProvider;
    // Cache verifiers by key ID (kid)
    private final Map<String, JWTVerifier> verifierCache = new java.util.concurrent.ConcurrentHashMap<>();

    public CognitoAuthService(Vertx vertx, String userPoolId, String clientId, String region) {
        this.vertx = vertx;
        this.userPoolId = userPoolId;
        this.clientId = clientId;
        this.region = region;
        initializeJwkProvider();
    }

    private void initializeJwkProvider() {
        try {
            String jwksUrl = String.format("https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json", region,
                    userPoolId);
            this.jwkProvider = new JwkProviderBuilder(new URI(jwksUrl).toURL()).build();
        } catch (MalformedURLException | java.net.URISyntaxException e) {
            throw new RuntimeException("Failed to initialize JWK provider", e);
        }
    }

    public Future<JsonObject> validateToken(String token) {
        // Don't remove blocking calls, this will break authentication
        return vertx.executeBlocking(() -> {
            DecodedJWT jwt = JWT.decode(token);
            String kid = jwt.getKeyId();

            JWTVerifier verifier = verifierCache.computeIfAbsent(kid, k -> {
                try {
                    RSAPublicKey publicKey = (RSAPublicKey) jwkProvider.get(k).getPublicKey();
                    Algorithm algorithm = Algorithm.RSA256(publicKey, null);
                    return JWT.require(algorithm)
                            .withIssuer(
                                    String.format("https://cognito-idp.%s.amazonaws.com/%s", region, userPoolId))
                            .build();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to build JWT verifier for kid: " + k, e);
                }
            });

            DecodedJWT verifiedJwt = verifier.verify(token);

            // Accept if either aud or client_id matches clientId
            boolean audOk = verifiedJwt.getAudience() != null && verifiedJwt.getAudience().contains(clientId);
            boolean clientIdOk = clientId.equals(verifiedJwt.getClaim("client_id").asString());

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

            logger.debug("token validated, username: {}", userInfo.getString("username"));
            return userInfo;
        });
    }

    public void close() {
        // JwkProvider and verifier cache don't require explicit cleanup
        logger.debug("CognitoAuthService closed");
    }
}
