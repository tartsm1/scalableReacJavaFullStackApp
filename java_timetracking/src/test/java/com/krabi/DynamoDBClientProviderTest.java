package com.krabi;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

class DynamoDBClientProviderTest {

    @Test
    void getClient_ShouldReturnNonNullClient() {
        DynamoDbClient client = DynamoDBClientProvider.getClient();
        assertNotNull(client);
    }

    @Test
    void getClient_ShouldReturnSameInstance_OnMultipleCalls() {
        DynamoDbClient client1 = DynamoDBClientProvider.getClient();
        DynamoDbClient client2 = DynamoDBClientProvider.getClient();
        assertSame(client1, client2);
    }
}
