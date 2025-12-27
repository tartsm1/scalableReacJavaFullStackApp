package com.krabi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

public class TaskService {

    private final DynamoDbClient dynamoDbClient;
    private final String tableName = "Tasks";
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    public TaskService(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public void createTask(Task task) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().n(Long.toString(task.getId())).build());
        item.put("date", AttributeValue.builder().s(task.getDate()).build());
        item.put("project", AttributeValue.builder().s(task.getProject()).build());
        item.put("hours", AttributeValue.builder().n(Integer.toString(task.getHours())).build());
        item.put("task", AttributeValue.builder().s(task.getTask()).build());
        if (task.getUsername() != null) {
            item.put("username", AttributeValue.builder().s(task.getUsername()).build());
        }
        logger.info("Creating task: {}", item);
        PutItemRequest request = PutItemRequest.builder().tableName(tableName).item(item).build();
        logger.info("Request: {}", request);
        try {
            dynamoDbClient.putItem(request);
        } catch (DynamoDbException e) {
            logDynamoDbError(e);
        }
    }

    public Task getTask(long id) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", AttributeValue.builder().n(Long.toString(id)).build());
        GetItemRequest request = GetItemRequest.builder().tableName(tableName).key(key).build();
        Map<String, AttributeValue> item = dynamoDbClient.getItem(request).item();
        if (item == null || item.isEmpty()) {
            return null;
        }
        return fromItem(item);
    }

    public List<Task> listTasks(String userNameFromCtx) {
        // in real production app use always Query instead Scan!!!
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":username", AttributeValue.builder().s(userNameFromCtx).build());
        ScanRequest request = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression("username = :username")
                .expressionAttributeValues(expressionAttributeValues)
                .build();
        List<Task> tasks = new ArrayList<>();
        try {
            List<Map<String, AttributeValue>> items = dynamoDbClient.scan(request).items();
            for (Map<String, AttributeValue> item : items) {
                tasks.add(fromItem(item));
            }
        } catch (DynamoDbException e) {
            logDynamoDbError(e);
        }
        return tasks;
    }

    public void updateTask(Task task) {
        createTask(task); // Overwrites existing item
    }

    public void deleteTask(long id) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", AttributeValue.builder().n(Long.toString(id)).build());
        DeleteItemRequest request = DeleteItemRequest.builder().tableName(tableName).key(key).build();
        try {
            dynamoDbClient.deleteItem(request);
        } catch (DynamoDbException e) {
            logDynamoDbError(e);
        }
    }

    private Task fromItem(Map<String, AttributeValue> item) {
        long id = Long.parseLong(item.get("id").n());
        String date = item.get("date").s();
        String project = item.get("project").s();
        int hours = Integer.parseInt(item.get("hours").n());
        String task = item.get("task").s();
        String username = item.containsKey("username") ? item.get("username").s() : null;
        return new Task(id, date, project, hours, task, username);
    }

    private void logDynamoDbError(DynamoDbException e) {
        // THIS IS THE CRUCIAL PART FOR DEBUGGING
        logger.error("----------- DYNAMODB ERROR -----------");
        logger.error("Error Message: {}", e.awsErrorDetails().errorMessage());
        logger.error("AWS Error Code: {}", e.awsErrorDetails().errorCode());
        logger.error("SDK Error Message: {}", e.getMessage());
        logger.error("Request ID: {}", e.requestId());
        logger.error("Status Code: {}", e.statusCode());
        logger.error("------------------------------------");
        throw new RuntimeException(e.awsErrorDetails().errorMessage());
    }
}
