package com.krabi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
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
        assertEquals(1L, result.getId());
        assertEquals("Project A", result.getProject());
    }

    @Test
    void getTask_ShouldReturnNull_WhenItemDoesNotExist() {
        GetItemResponse response = GetItemResponse.builder().item(new HashMap<>()).build();
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(response);

        Task result = taskService.getTask(1L);

        assertNull(result);
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
        assertEquals("Project A", results.get(0).getProject());
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
}
