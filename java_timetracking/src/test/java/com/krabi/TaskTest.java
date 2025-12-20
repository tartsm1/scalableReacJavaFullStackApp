package com.krabi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class TaskTest {

    @Test
    void testTaskConstructorAndGetters() {
        Task task = new Task(1L, "2023-10-27", "Project A", 8, "Coding", "user1");
        
        assertEquals(1L, task.getId());
        assertEquals("2023-10-27", task.getDate());
        assertEquals("Project A", task.getProject());
        assertEquals(8, task.getHours());
        assertEquals("Coding", task.getTask());
        assertEquals("user1", task.getUsername());
    }

    @Test
    void testTaskSetters() {
        Task task = new Task();
        task.setId(2L);
        task.setDate("2023-10-28");
        task.setProject("Project B");
        task.setHours(4);
        task.setTask("Meeting");
        task.setUsername("user2");

        assertEquals(2L, task.getId());
        assertEquals("2023-10-28", task.getDate());
        assertEquals("Project B", task.getProject());
        assertEquals(4, task.getHours());
        assertEquals("Meeting", task.getTask());
        assertEquals("user2", task.getUsername());
    }
    
    @Test
    void testConstructorWithoutId() {
        Task task = new Task("2023-10-29", "Project C", 5, "Design", "user3");
        
        assertEquals("2023-10-29", task.getDate());
        assertEquals("Project C", task.getProject());
        assertEquals(5, task.getHours());
        assertEquals("Design", task.getTask());
        assertEquals("user3", task.getUsername());
    }
}
