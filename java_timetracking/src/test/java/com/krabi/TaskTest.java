package com.krabi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class TaskTest {

    @Test
    void testTaskConstructorAndGetters() {
        Task task = new Task(1L, "2023-10-27", "Project A", 8, "Coding", "user1");
        
        assertEquals(1L, task.id());
        assertEquals("2023-10-27", task.date());
        assertEquals("Project A", task.project());
        assertEquals(8, task.hours());
        assertEquals("Coding", task.task());
        assertEquals("user1", task.username());
    }

    @Test
    void testWithMethods() {
        Task task = new Task(0, "2023-10-28", "Project B", 4, "Meeting", null);

        Task withId = task.withId(2L);
        assertEquals(2L, withId.id());
        assertEquals("2023-10-28", withId.date());
        assertEquals("Project B", withId.project());
        assertEquals(4, withId.hours());
        assertEquals("Meeting", withId.task());

        Task withUsername = task.withUsername("user2");
        assertEquals("user2", withUsername.username());
        assertEquals(0, withUsername.id()); // original id unchanged

        Task withBoth = task.withIdAndUsername(3L, "user3");
        assertEquals(3L, withBoth.id());
        assertEquals("user3", withBoth.username());
        assertEquals("Project B", withBoth.project()); // other fields preserved
    }
    
    @Test
    void testConstructorWithoutId() {
        Task task = new Task("2023-10-29", "Project C", 5, "Design", "user3");
        
        assertEquals(0, task.id());
        assertEquals("2023-10-29", task.date());
        assertEquals("Project C", task.project());
        assertEquals(5, task.hours());
        assertEquals("Design", task.task());
        assertEquals("user3", task.username());
    }

    @Test
    void testEquals_SameValues() {
        Task task1 = new Task(1L, "2023-10-27", "Project A", 8, "Coding", "user1");
        Task task2 = new Task(1L, "2023-10-27", "Project A", 8, "Coding", "user1");
        assertEquals(task1, task2);
    }

    @Test
    void testEquals_DifferentValues() {
        Task task1 = new Task(1L, "2023-10-27", "Project A", 8, "Coding", "user1");
        Task task2 = new Task(2L, "2023-10-28", "Project B", 4, "Meeting", "user2");
        assertNotEquals(task1, task2);
    }

    @Test
    void testEquals_SameReference() {
        Task task = new Task(1L, "2023-10-27", "Project A", 8, "Coding", "user1");
        assertEquals(task, task);
    }

    @Test
    void testEquals_Null() {
        Task task = new Task(1L, "2023-10-27", "Project A", 8, "Coding", "user1");
        assertNotEquals(null, task);
    }

    @Test
    void testHashCode_SameValues() {
        Task task1 = new Task(1L, "2023-10-27", "Project A", 8, "Coding", "user1");
        Task task2 = new Task(1L, "2023-10-27", "Project A", 8, "Coding", "user1");
        assertEquals(task1.hashCode(), task2.hashCode());
    }

    @Test
    void testToString_ContainsFields() {
        Task task = new Task(1L, "2023-10-27", "Project A", 8, "Coding", "user1");
        String str = task.toString();
        assertTrue(str.contains("1"));
        assertTrue(str.contains("2023-10-27"));
        assertTrue(str.contains("Project A"));
        assertTrue(str.contains("8"));
        assertTrue(str.contains("Coding"));
        assertTrue(str.contains("user1"));
    }

    @Test
    void testImmutability() {
        Task original = new Task(1L, "2023-10-27", "Project A", 8, "Coding", "user1");
        Task copy = original.withId(99L);

        // Original should be unchanged
        assertEquals(1L, original.id());
        assertEquals(99L, copy.id());
        // Other fields should be the same
        assertEquals(original.date(), copy.date());
        assertEquals(original.project(), copy.project());
    }
}
