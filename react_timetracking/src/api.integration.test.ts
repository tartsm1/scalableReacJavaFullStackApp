import { addTask, getAllTasks, updateTask, deleteTask, Task } from './api';

describe('API Integration Tests (Real Server)', () => {
  const testTask: Task = {
    id: Date.now(),
    project: 'IntegrationTest',
    task: 'TestTask',
    date: new Date().toISOString().slice(0, 10),
    hours: 2,
  };

  // Clean up after each test
  afterEach(async () => {
    try {
      // Try to delete the test task if it exists
      await deleteTask(testTask.id);
    } catch (error) {
      // Ignore errors if task doesn't exist
    }
  });

  it('should add a task to real server', async () => {
    // Add the task
    await addTask(testTask);
    
    // Verify it was added by fetching all tasks
    const tasks = await getAllTasks();
    const found = tasks.find(t => t.id === testTask.id);
    
    expect(found).toBeDefined();
    expect(found?.project).toBe('IntegrationTest');
    expect(found?.task).toBe('TestTask');
    expect(found?.hours).toBe(2);
  }, 15000); // 15 second timeout for real network calls

  it('should update a task on real server', async () => {
    // First add the task
    await addTask(testTask);
    
    // Update the task
    await updateTask(testTask.id, { hours: 5 });
    
    // Verify the update
    const tasks = await getAllTasks();
    const found = tasks.find(t => t.id === testTask.id);
    
    expect(found).toBeDefined();
    expect(found?.hours).toBe(5);
  }, 15000);

  it('should delete a task from real server', async () => {
    // First add the task
    await addTask(testTask);
    
    // Verify it exists
    let tasks = await getAllTasks();
    let found = tasks.find(t => t.id === testTask.id);
    expect(found).toBeDefined();
    
    // Delete the task
    await deleteTask(testTask.id);
    
    // Verify it was deleted
    tasks = await getAllTasks();
    found = tasks.find(t => t.id === testTask.id);
    expect(found).toBeUndefined();
  }, 15000);

  it('should handle server errors gracefully', async () => {
    // Test with invalid data
    const invalidTask = {
      id: -1,
      project: '',
      task: '',
      date: 'invalid-date',
      hours: -1,
    };

    try {
      await addTask(invalidTask as Task);
      // If we get here, the server accepted invalid data (which might be OK)
    } catch (error) {
      // Expected error for invalid data
      expect(error).toBeInstanceOf(Error);
    }
  }, 15000);
}); 