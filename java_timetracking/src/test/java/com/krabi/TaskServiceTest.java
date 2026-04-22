package com.krabi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private DynamoDbClient dynamoDbClient;

    private TaskService taskService;

    @BeforeEach
    public void setUp() {
        taskService = new TaskService(dynamoDbClient);
    }

    private DynamoDbException createDynamoDbException(String message) {
        return (DynamoDbException) DynamoDbException.builder()
                .message(message)
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorMessage(message)
                        .errorCode("TestError")
                        .serviceName("DynamoDB")
                        .build())
                .statusCode(400)
                .build();
    }

    @Test
    void createTask_ShouldPutItem() {
        Task task = new Task(1L, "2023-10-27", "Project A", 8, "Coding", "user1");

        taskService.createTask(task);

        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(captor.capture());
        PutItemRequest request = captor.getValue();

        assertEquals("Tasks", request.tableName());
        Map<String, AttributeValue> item = request.item();
        assertEquals("1", item.get("id").n());
        assertEquals("2023-10-27", item.get("date").s());
        assertEquals("Project A", item.get("project").s());
        assertEquals("8", item.get("hours").n());
        assertEquals("Coding", item.get("task").s());
        assertEquals("user1", item.get("username").s());
    }

    @Test
    void createTask_ShouldNotGenerateId_WhenIdIsZero() {
        Task task = new Task("2023-10-27", "Project A", 8, "Coding", "user1");
        assertEquals(0, task.id());

        Task created = taskService.createTask(task);

        assertEquals(0, created.id(), "ID should be not auto-generated when 0, DynamoDb will set it");

        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(captor.capture());
        PutItemRequest request = captor.getValue();
        assertEquals(Long.toString(created.id()), request.item().get("id").n());
    }

    @Test
    void createTask_ShouldOmitUsername_WhenUsernameIsNull() {
        Task task = new Task(2L, "2023-10-28", "Project B", 4, "Meeting", null);

        taskService.createTask(task);

        ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
        verify(dynamoDbClient).putItem(captor.capture());
        PutItemRequest request = captor.getValue();

        Map<String, AttributeValue> item = request.item();
        assertNull(item.get("username"));
        assertEquals("2", item.get("id").n());
    }

    @Test
    void createTask_ShouldThrowRuntimeException_WhenDynamoDbException() {
        Task task = new Task(1L, "2023-10-27", "Project A", 8, "Coding", "user1");
        DynamoDbException dbException = createDynamoDbException("Create failed");
        when(dynamoDbClient.putItem(any(PutItemRequest.class))).thenThrow(dbException);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> taskService.createTask(task));
        assertEquals("Create failed", thrown.getMessage());
    }

    @Test
    void getTask_ShouldReturnTask_WhenItemExists() {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().n("1").build());
        item.put("date", AttributeValue.builder().s("2023-10-27").build());
        item.put("project", AttributeValue.builder().s("Project A").build());
        item.put("hours", AttributeValue.builder().n("8").build());
        item.put("task", AttributeValue.builder().s("Coding").build());
        item.put("username", AttributeValue.builder().s("user1").build());

        GetItemResponse response = GetItemResponse.builder().item(item).build();
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(response);

        Task result = taskService.getTask(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Project A", result.project());
        assertEquals("user1", result.username());
    }

    @Test
    void getTask_ShouldReturnNull_WhenItemDoesNotExist() {
        GetItemResponse response = GetItemResponse.builder().item(new HashMap<>()).build();
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(response);

        Task result = taskService.getTask(1L);

        assertNull(result);
    }

    @Test
    void getTask_ShouldReturnNull_WhenItemIsNull() {
        GetItemResponse response = GetItemResponse.builder().build();
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(response);

        Task result = taskService.getTask(1L);

        assertNull(result);
    }

    @Test
    void getTask_ShouldReturnTask_WithNullUsername_WhenUsernameKeyMissing() {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().n("5").build());
        item.put("date", AttributeValue.builder().s("2023-11-01").build());
        item.put("project", AttributeValue.builder().s("Project X").build());
        item.put("hours", AttributeValue.builder().n("3").build());
        item.put("task", AttributeValue.builder().s("Review").build());
        // No "username" key

        GetItemResponse response = GetItemResponse.builder().item(item).build();
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(response);

        Task result = taskService.getTask(5L);

        assertNotNull(result);
        assertEquals(5L, result.id());
        assertNull(result.username());
    }

    @Test
    void listTasks_ShouldReturnListOfTasks() {
        Map<String, AttributeValue> item1 = new HashMap<>();
        item1.put("id", AttributeValue.builder().n("1").build());
        item1.put("date", AttributeValue.builder().s("2023-10-27").build());
        item1.put("project", AttributeValue.builder().s("Project A").build());
        item1.put("hours", AttributeValue.builder().n("8").build());
        item1.put("task", AttributeValue.builder().s("Coding").build());
        item1.put("username", AttributeValue.builder().s("user1").build());

        ScanResponse response = ScanResponse.builder().items(List.of(item1)).build();
        when(dynamoDbClient.scan(any(ScanRequest.class))).thenReturn(response);

        List<Task> results = taskService.listTasks("user1");

        assertEquals(1, results.size());
        assertEquals("Project A", results.get(0).project());
    }

    @Test
    void listTasks_ShouldReturnEmptyList_WhenNullUsername() {
        List<Task> results = taskService.listTasks(null);

        assertTrue(results.isEmpty());
        // Should not even call DynamoDB
        verify(dynamoDbClient, never()).scan(any(ScanRequest.class));
    }

    @Test
    void listTasks_ShouldThrowRuntimeException_WhenDynamoDbException() {
        DynamoDbException dbException = createDynamoDbException("Scan failed");
        when(dynamoDbClient.scan(any(ScanRequest.class))).thenThrow(dbException);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> taskService.listTasks("user1"));
        assertEquals("Scan failed", thrown.getMessage());
    }

    @Test
    void updateTask_ShouldPutItem() {
        Task task = new Task(1L, "2023-10-27", "Project A", 8, "Coding", "user1");

        taskService.updateTask(task);

        verify(dynamoDbClient).putItem(any(PutItemRequest.class));
    }

    @Test
    void deleteTask_ShouldDeleteItem() {
        taskService.deleteTask(1L);

        ArgumentCaptor<DeleteItemRequest> captor = ArgumentCaptor.forClass(DeleteItemRequest.class);
        verify(dynamoDbClient).deleteItem(captor.capture());
        DeleteItemRequest request = captor.getValue();

        assertEquals("Tasks", request.tableName());
        assertEquals("1", request.key().get("id").n());
    }

    @Test
    void deleteTask_ShouldThrowRuntimeException_WhenDynamoDbException() {
        DynamoDbException dbException = createDynamoDbException("Delete failed");
        when(dynamoDbClient.deleteItem(any(DeleteItemRequest.class))).thenThrow(dbException);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> taskService.deleteTask(1L));
        assertEquals("Delete failed", thrown.getMessage());
    }

    @Test
    void listTasks_ShouldReturnEmptyList_WhenNoTasks() {
        ScanResponse response = ScanResponse.builder().items(List.of()).build();
        when(dynamoDbClient.scan(any(ScanRequest.class))).thenReturn(response);

        List<Task> results = taskService.listTasks("user1");

        assertTrue(results.isEmpty());
    }
}
