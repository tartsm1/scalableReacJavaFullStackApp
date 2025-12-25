package com.krabi;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDBClientProvider {

    private static final Logger logger = LoggerFactory.getLogger(DynamoDBClientProvider.class);

    private static DynamoDbClient client;

    public static DynamoDbClient getClient() {
        if (client == null) {

            String endpoint = System.getenv("DYNAMODB_ENDPOINT");
            String regionEnv = System.getenv("AWS_REGION");
            Region region = (regionEnv != null) ? Region.of(regionEnv) : Region.EU_NORTH_1;
            logger.info("endpoint: {}", endpoint);
            logger.info("region: {}", region);

            var builder = DynamoDbClient.builder().region(region);

            if (endpoint != null) {
                // Local/dev: use dummy credentials
                builder = builder
                        .endpointOverride(URI.create(endpoint))
                        .credentialsProvider(DefaultCredentialsProvider.builder().build());
            } else {
                // AWS: use default provider chain (supports EC2 IAM roles)
                builder = builder.credentialsProvider(DefaultCredentialsProvider.builder().build());
            }
            client = builder.build();
        }
        return client;
    }
}
