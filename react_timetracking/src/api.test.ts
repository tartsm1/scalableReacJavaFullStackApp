// Mock fetch globally for tests
global.fetch = jest.fn();

// Import the API functions directly without importing React files
const API_BASE_URL = 'http://localhost:8888/api';

interface Task {
  id: number;
  project: string;
  task: string;
  date: string;
  hours: number;
}

async function getAllTasks(): Promise<Task[]> {
  const response = await fetch(`${API_BASE_URL}/tasks`);
  if (!response.ok) {
    throw new Error(`Failed to fetch tasks: ${response.statusText}`);
  }
  return response.json();
}

async function addTask(task: Task): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/tasks`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(task),
  });
  if (!response.ok) {
    throw new Error(`Failed to add task: ${response.statusText}`);
  }
}

async function updateTask(id: number, updates: Partial<Task>): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/tasks/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(updates),
  });
  if (!response.ok) {
    throw new Error(`Failed to update task: ${response.statusText}`);
  }
}

async function deleteTask(id: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/tasks/${id}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    throw new Error(`Failed to delete task: ${response.statusText}`);
  }
}

describe('API Task CRUD', () => {
  const testTask: Task = {
    id: Date.now(),
    project: 'TestProject',
    task: 'TestTask',
    date: new Date().toISOString().slice(0, 10),
    hours: 2,
  };

  beforeEach(() => {
    (fetch as jest.Mock).mockClear();
  });

  it('should add a task', async () => {
    (fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => [],
    });

    await addTask(testTask);
    expect(fetch).toHaveBeenCalledWith(`${API_BASE_URL}/tasks`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(testTask),
    });
  });

  it('should get all tasks', async () => {
    const mockTasks = [testTask];
    (fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
      json: async () => mockTasks,
    });

    const tasks = await getAllTasks();
    expect(tasks).toEqual(mockTasks);
    expect(fetch).toHaveBeenCalledWith(`${API_BASE_URL}/tasks`);
  });

  it('should update a task', async () => {
    (fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
    });

    await updateTask(testTask.id, { hours: 5 });
    expect(fetch).toHaveBeenCalledWith(`${API_BASE_URL}/tasks/${testTask.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ hours: 5 }),
    });
  });

  it('should delete a task', async () => {
    (fetch as jest.Mock).mockResolvedValueOnce({
      ok: true,
    });

    await deleteTask(testTask.id);
    expect(fetch).toHaveBeenCalledWith(`${API_BASE_URL}/tasks/${testTask.id}`, {
      method: 'DELETE',
    });
  });
}); 