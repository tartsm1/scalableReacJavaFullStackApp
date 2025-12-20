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
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;

public class CognitoAuthService {

    private static final Logger logger = LoggerFactory.getLogger(CognitoAuthService.class);
    private final String userPoolId;
    private final String clientId;
    private final String region;
    private final CognitoIdentityProviderClient cognitoClient;
    private final Vertx vertx;
    private JwkProvider jwkProvider;
    private final Map<Algorithm, JWTVerifier> algorithmMap = new java.util.concurrent.ConcurrentHashMap<>();

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
            this.jwkProvider = new JwkProviderBuilder(new URI(jwksUrl).toURL()).build();
        } catch (MalformedURLException | java.net.URISyntaxException e) {
            throw new RuntimeException("Failed to initialize JWK provider", e);
        }
    }

    public Future<JsonObject> validateToken(String token) {
        // Don't remove blocking calls, this will break authentification
        return vertx.executeBlocking(() -> {
            DecodedJWT jwt = JWT.decode(token);
            // Verify the token signature
            RSAPublicKey publicKey = (RSAPublicKey) jwkProvider.get(jwt.getKeyId()).getPublicKey();
            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            JWTVerifier verifier = algorithmMap.computeIfAbsent(algorithm, alg -> JWT.require(alg)
                    .withIssuer(String.format("https://cognito-idp.%s.amazonaws.com/%s", region, userPoolId))
                    .build());
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

            logger.info("token validated, username: {}", userInfo.getString("username"));
            return userInfo;
        });
    }

    // this method authenticateUser not used as authentication handled by frontend
    public Future<JsonObject> authenticateUser(String username, String password) {
        return vertx.executeBlocking(() -> {
            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(clientId)
                    .authParameters(Map.of("USERNAME", username, "PASSWORD", password))
                    .build();

            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);

            if (authResponse.authenticationResult() != null) {
                return new JsonObject()
                        .put("accessToken", authResponse.authenticationResult().accessToken())
                        .put("idToken", authResponse.authenticationResult().idToken())
                        .put("refreshToken", authResponse.authenticationResult().refreshToken());
            } else {
                throw new RuntimeException("Authentication failed");
            }
        });
    }

    public void close() {
        if (cognitoClient != null) {
            cognitoClient.close();
        }
    }
}
