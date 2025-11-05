import { fetchAuthSession, getCurrentUser } from 'aws-amplify/auth';

export interface Task {
  id: number;
  project: string;
  task: string;
  date: string;
  hours: number;
}

// Use relative URLs to send requests to the same host/port as the app
const API_BASE_URL = '/api';

async function getAuthHeaders(contentType: string | null = null): Promise<HeadersInit> {
  try {
    const session = await fetchAuthSession();
    const idToken = session.tokens?.idToken?.toString();
    let username = undefined;
    try {
      const user = await getCurrentUser();
      username = user.username;
    } catch {}
    const headers: HeadersInit = {};
    if (idToken) {
      headers['Authorization'] = `Bearer ${idToken}`;
    }
    if (username) {
      headers['X-Username'] = username;
    }
    if (contentType) {
      headers['Content-Type'] = contentType;
    }
    return headers;
  } catch (e) {
    // Not authenticated, return only content-type if needed
    return contentType ? { 'Content-Type': contentType } : {};
  }
}

export async function getAllTasks(): Promise<Task[]> {
  const response = await fetch(`${API_BASE_URL}/tasks`, {
    headers: await getAuthHeaders(),
  });
  if (!response.ok) {
    throw new Error(`Failed to fetch tasks: ${response.statusText}`);
  }
  return response.json();
}

export async function addTask(task: Task): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/tasks`, {
    method: 'POST',
    headers: await getAuthHeaders('application/json'),
    body: JSON.stringify(task),
  });
  if (!response.ok) {
    throw new Error(`Failed to add task: ${response.statusText}`);
  }
}

export async function updateTask(id: number, updates: Partial<Task>): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/tasks/${id}`, {
    method: 'PUT',
    headers: await getAuthHeaders('application/json'),
    body: JSON.stringify(updates),
  });
  if (!response.ok) {
    throw new Error(`Failed to update task: ${response.statusText}`);
  }
}

export async function deleteTask(id: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/tasks/${id}`, {
    method: 'DELETE',
    headers: await getAuthHeaders(),
  });
  if (!response.ok) {
    throw new Error(`Failed to delete task: ${response.statusText}`);
  }
} 